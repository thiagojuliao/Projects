import integrations.Databricks._
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

package object integrations {

  def getSparkInfo(spark: SparkSession): Map[String, Any] = {
    val sparkInfo = mutable.Map[String, Any]()

    val clusterId = getClusterId(spark)
    val clusterName = getClusterName(spark)
    val sparkContextId = getSparkContextId(spark)
    val applicationId = getApplicationId(spark)
    val applicationName = getApplicationName(spark)

    sparkInfo += "clusterId" -> clusterId
    sparkInfo += "clusterName" -> clusterName
    sparkInfo += "sparkContextId" -> sparkContextId
    sparkInfo += "applicationId" -> applicationId
    sparkInfo += "applicationName" -> applicationName

    sparkInfo.toMap
  }

  def getIntegrationInfo: Map[String, Any] =
    ConfigFactory.load().getConfig("spark-data-lineage-listener").getString("integration") match {
      // TODO: Every new integration must be pattern matched here
      case "databricks" => Databricks.getIntegrationInfo

      case "debug" => Map("mode" -> "debug")

      case integration =>
        throw new IllegalArgumentException(s"No previous pipeline found for this kind of integration: $integration")
    }
}
