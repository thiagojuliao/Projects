package core

import dsl._
import integrations._
import integrations.pipelines.implicits._
import org.apache.spark.internal.Logging
import org.apache.spark.sql.execution._
import org.apache.spark.sql.util.QueryExecutionListener

import akka.actor.{ActorRef, ActorSystem, Props}
import java.util.UUID

class SparkDataLineageListener extends QueryExecutionListener with Logging {
  import QueryExecutionData._

  private val system = ActorSystem(s"SparkDataLineageListener_${UUID.randomUUID()}")
  implicit val httpRequester: ActorRef = system.actorOf(Props[HttpRequester], s"HttpRequester_${UUID.randomUUID()}")

  private val integrationInfo: Map[String, Any] = getIntegrationInfo

  private def processQueryExecutionData(funcName: String, qe: QueryExecution, duration: Long, status: String, exception: Option[String]): Unit = {
    val queryId = UUID.randomUUID().toString

    val spark = qe.sparkSession

    val sparkInfo = getSparkInfo(spark)
    implicit val info: Map[String, Any] = integrationInfo ++ sparkInfo

    val statusStore = spark.sharedState.statusStore
    val executionId = statusStore.executionsList().last.executionId
    val jobs = statusStore.execution(executionId).get.jobs.keys.toList
    val stages = statusStore.execution(executionId).get.stages.toList
    val metricValues = statusStore.executionMetrics(executionId)

    log.info(s"Processing lineage data for query $queryId")

    val spi = SparkPlanInfo.fromSparkPlan(qe.executedPlan)
    val data = SparkLineageData(spi, metricValues)
    val qeData = QueryExecutionData(queryId, executionId, funcName, duration, status, exception, jobs, stages, data)

    log.info("Running pipeline...")
    qeData.pipe(qe).run()
  }

  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit =
    processQueryExecutionData(funcName, qe, durationNs, SUCCEEDED, None)

  override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit =
    processQueryExecutionData(funcName, qe, -1L, FAILED, Some(exception.getMessage))
}
