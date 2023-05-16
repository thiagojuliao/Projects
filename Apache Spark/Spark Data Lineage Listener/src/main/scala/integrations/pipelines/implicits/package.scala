package integrations.pipelines

import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import dsl.QueryExecutionData
import integrations.pipelines.databricks.DatabricksSQLStatementAPI
import integrations.pipelines.debug.DebugConsole
import org.apache.spark.sql.execution.QueryExecution

package object implicits {

  implicit class PipeTo(data: QueryExecutionData)(implicit integrationInfo: Map[String, Any], httpRequester: ActorRef) {

    private val config = ConfigFactory.load().getConfig("spark-data-lineage-listener")

    def pipe(qe: QueryExecution): RunnablePipeline = config.getString("integration") match {
      case "databricks" =>
        config.getString("pipeline") match {
          case "sql-statement-execution-api" => DatabricksSQLStatementAPI(integrationInfo, data, httpRequester)

          case pipeline =>
            throw new IllegalArgumentException(s"$pipeline is not a valid registered pipeline. Please check your application.conf file for any inconsistencies.")
      }

      case "debug" =>
        config.getString("pipeline") match {
          case "console" => DebugConsole(qe)

          case pipeline =>
            throw new IllegalArgumentException(s"$pipeline is not a valid registered pipeline. Please check your application.conf file for any inconsistencies.")
        }

      case integration =>
        throw new IllegalArgumentException(s"$integration is not a valid registered integration. Please check your application.conf file for any inconsistencies.")
    }
  }
}
