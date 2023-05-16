package integrations

import com.typesafe.config.Config
import org.apache.spark.sql.SparkSession

import scala.collection.JavaConverters._

trait Integration {
  val config: Config

  lazy val props: Map[String, String] = config.entrySet().asScala.map { entry =>
    entry.getKey -> entry.getValue.unwrapped().toString
  }.toMap

  def getIntegrationInfo: Map[String, Any]
}
