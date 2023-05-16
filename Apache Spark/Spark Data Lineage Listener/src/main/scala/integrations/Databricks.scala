package integrations

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.util.{Success, Try}

object Databricks extends Integration {

  override val config: Config =
    ConfigFactory.load().getConfig("spark-data-lineage-listener.integrations.databricks")

  private[integrations] def getClusterId(spark: SparkSession): Option[String] =
    spark.conf.getOption("spark.Databricks.clusterUsageTags.clusterId")

  private[integrations] def getClusterName(spark: SparkSession): Option[String] =
    spark.conf.getOption("spark.Databricks.clusterUsageTags.clusterName")

  private[integrations] def getSparkContextId(spark: SparkSession): Option[String] =
    spark.conf.getOption("spark.Databricks.sparkContextId")

  private[integrations] def getApplicationId(spark: SparkSession): String = spark.sparkContext.applicationId

  private[integrations] def getApplicationName(spark: SparkSession): String = spark.sparkContext.appName

  private def getNotebookPath: Option[String] = Try(dbutils.notebook.getContext().notebookPath).toOption.flatten

  private def getUserName: Option[String] = Try(dbutils.notebook.getContext().userName).toOption.flatten

  private def getApiToken: Option[String] = {
    // Tries to fetch the authentication token from the Config File (only for development purposes)
    Try(config.getString("auth-token")) match {
      case Success(token) => Some(token)

      case _ =>
        // Fallback to fetching from the dbutils instance instead (production environment)
        Try(dbutils.notebook.getContext().apiToken).toOption.flatten
    }
  }

  override def getIntegrationInfo: Map[String, Any] = {
    val integrationInfo = mutable.Map[String, Any]()

    val notebookPath = getNotebookPath
    val username = getUserName
    val apiToken = getApiToken.getOrElse("TokenNotFound")

    integrationInfo += "notebookPath" -> notebookPath
    integrationInfo += "username" -> username
    integrationInfo += "apiToken" -> apiToken

    integrationInfo.toMap
  }
}
