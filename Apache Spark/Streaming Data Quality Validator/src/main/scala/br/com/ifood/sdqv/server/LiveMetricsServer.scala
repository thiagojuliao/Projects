package br.com.ifood.sdqv.server

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import br.com.ifood.sdqv.core.GeneralTypes._
import br.com.ifood.sdqv.core.StreamingDataQualityValidator
import br.com.ifood.sdqv.utils.PersistenceUtils.{METRICS_TABLE, createMetricsTableIfNotExists, saveMetrics}
import br.com.ifood.sdqv.utils.StreamingDataQualityValidatorUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object LiveMetricsServer {
  import LiveMetricsServerDomain._

  private val system = ActorSystem(s"StreamingDataQualityValidator-${UUID.randomUUID()}")
  private var liveMetricsServer: ActorRef = system.deadLetters
  private val logger = LoggerFactory.getLogger(system.name)

  implicit val timeout: Timeout = Timeout(5.seconds)

  def apply(sdqv: StreamingDataQualityValidator): Props =
    Props(new LiveMetricsServer(sdqv))

  def initialize(sdqv: StreamingDataQualityValidator): Unit = {
    liveMetricsServer = system.actorOf(this.apply(sdqv), s"LiveMetricsServer-${UUID.randomUUID()}")
  }

  def stop(): Unit =
    system.stop(liveMetricsServer)

  def saveMetrics(dataframe: DataFrame): Unit = {
    import system.dispatcher

    val futureResponse = liveMetricsServer ? SaveMetrics(dataframe)

    futureResponse.onComplete {
      case Success(value) =>
        logger.info(value.toString)
      case Failure(_) =>
        logger.warn("Could not save metrics because there is no Live Metrics Server running at this time.\nPlease run .initialize() to start a new one.")
    }
  }
}

class LiveMetricsServer(sdqv: StreamingDataQualityValidator) extends Actor with ActorLogging with Timers {
  import LiveMetricsServerDomain._
  import StreamingDataQualityValidatorUtils._

  private implicit val spark: SparkSession = SparkSession.builder().getOrCreate()
  private implicit val ec: ExecutionContext = context.system.dispatcher

  timers.startTimerAtFixedRate(TimerKey, CheckForStreamingProgress, 1.second)

  override def preStart(): Unit =
    log.info(s"Initializing Live Metrics Server")

  override def postStop(): Unit =
    log.info("Live Metrics Server shutdown complete")

  override def receive: Receive = online(sdqv)

  def online(sdqv: StreamingDataQualityValidator, currentBatchId: Long = -1): Receive = {
    case CheckForStreamingProgress =>
      val validations: Validations = sdqv.validations
      val processInfo: ProcessInfo = sdqv.processInfo
      val lastBatchInfo: (Long, String) = getLastBatchInfo(processInfo)
      val lastBatchId: Long = lastBatchInfo._1

      if (lastBatchId >= 0 & lastBatchId > currentBatchId) {
        log.info(s"New batch found. Saving data quality metrics for batch $lastBatchId")

        val metricsValues: MetricsValues = sdqv.getMetricsValues
        val totalRecords: Long = sdqv.getTotalRecords

        sdqv.resetAccumulators()

        val saveMetricsResponse: Future[Long] = Future {
          saveMetrics(validations, processInfo, metricsValues, totalRecords, lastBatchInfo)

          lastBatchInfo._1
        }

        saveMetricsResponse.onComplete {
          case Success(_) =>
            log.info(s"Data quality metrics for batch $lastBatchId saved successfully")
          case Failure(ex) =>
            log.info(s"Failed to write data quality metrics for batch $lastBatchId. Reason: $ex")
        }
      }

      context.become(online(sdqv, lastBatchId))
    case SaveMetrics(dataframe) =>
      createMetricsTableIfNotExists()

      dataframe.write
        .format("delta")
        .mode("append")
        .partitionBy("type", "mode", "zone_stage", "namespace_product", "dataset", "dt")
        .saveAsTable(METRICS_TABLE)

      sender() ! "Metrics saved successfully"
  }

}