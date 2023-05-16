import br.com.ifood.sdqv.core.StreamingDataQualityValidator
import br.com.ifood.sdqv.server.LiveMetricsServer
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.when
import org.apache.spark.sql.streaming.{StreamingQuery, Trigger}

object TestOnStreamingData_2 extends App {

  val spark: SparkSession = SparkSession.builder()
    .appName("SDQV_TestOnStreaming_2")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  /**
   * Parametrization
   */
  val processInfo: Map[String, Any] = Map(
    "name" -> "prd-etl-curated-consumer-sessions",
    "type" -> "streaming",
    "mode" -> "data chef",
    "datasetInfo" -> Map(
      "zoneOrStage" -> "curated",
      "namespaceOrProduct" -> "consumer",
      "dataset" -> "sessions"
    )
  )

  val validations: Map[String, Any] = Map(
    "isNeverNull" -> List("id", "application_id", "device_id", "user_id", "dt"),
    "isAlwaysNull" -> List("properties_is_pwa"),
    "isMatchingRegex" -> Map(
      "id" -> "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$",
      "device_id" -> "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$",
      "user_id" -> "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$"
    ),
    "isAnyOf" -> Map(
      "application_id" -> List("ifood-android", "ifood-ios")
    ),
    "isFormattedAsDate" -> Map(
      "dt" -> "yyyy-MM-dd"
    )
  )

  /**
   * Sample Data
   */
  val data: DataFrame = spark.read
    .format("csv")
    .option("header", "true")
    .load("src/main/resources/data/")

  /**
   * Reading Stream Data
   */
  val streamData: DataFrame = spark.readStream
    .format("csv")
    .schema(data.schema)
    .option("header", "true")
    .option("maxFilesPerTrigger", 1)
    .load("src/main/resources/data/")
    .withColumn("properties_is_pwa", when($"properties_is_pwa" === "null", null).otherwise($"properties_is_pwa"))

  val sdqv: StreamingDataQualityValidator = StreamingDataQualityValidator(streamData, validations, processInfo)

  LiveMetricsServer.initialize(sdqv)

  val (cleanStreamData, dirtyStreamData): (DataFrame, DataFrame) = sdqv.validate(autoCleaning = true)

  val cleanQuery: StreamingQuery = cleanStreamData.writeStream
    .format("console")
    .trigger(Trigger.ProcessingTime("10 seconds"))
    .start()

  val dirtyQuery: StreamingQuery = dirtyStreamData.writeStream
    .format("console")
    .queryName("consumer_curated.sessions")
    .trigger(Trigger.ProcessingTime("10 seconds"))
    .start()

  Thread.sleep(30000)

  cleanQuery.stop()
  dirtyQuery.stop()

  LiveMetricsServer.stop()

  spark.read.table("default.data_quality_metrics")
    .orderBy("batch_id")
    .show(20, truncate = false)
}
