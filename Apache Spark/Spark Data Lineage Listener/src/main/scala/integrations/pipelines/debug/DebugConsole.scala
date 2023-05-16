package integrations.pipelines.debug

import core.SparkPlanInfo
import dsl.{QueryExecutionData, SparkLineageData}
import integrations.pipelines.RunnablePipeline
import org.apache.spark.internal.Logging
import org.apache.spark.sql.execution.QueryExecution

case class DebugConsole(qe: QueryExecution) extends RunnablePipeline with Logging {

  override val data: QueryExecutionData = null

  override def run(): Unit = {
    log.info("Printing query execution data to console...")

    val spark = qe.sparkSession
    val statusStore = spark.sharedState.statusStore
    val executionId = statusStore.executionsList().last.executionId
    val sparkPlanGraph = statusStore.planGraph(executionId)

    val metricValues = statusStore.executionMetrics(executionId)

    sparkPlanGraph.allNodes.foreach { node =>
      val nodeMetrics = node.metrics.map(spm => (spm.name, metricValues.getOrElse(spm.accumulatorId, null)))

      println(
        s"""
           |id := ${node.id}
           |name := ${node.name}
           |desc := ${node.desc}
           |metrics := $nodeMetrics
           |""".stripMargin
      )
    }

    val spi = SparkPlanInfo.fromSparkPlan(qe.executedPlan)
    val data = SparkLineageData(spi, metricValues)

    data.foreach(println)
  }
}
