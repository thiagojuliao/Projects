package integrations.pipelines.databricks

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import com.typesafe.config.ConfigFactory
import dsl.QueryExecutionData
import integrations.pipelines.RunnablePipeline
import json.implicits._
import org.apache.spark.internal.Logging
import spray.json._

import java.text.SimpleDateFormat
import scala.collection.immutable

case class DatabricksSQLStatementAPI(integrationInfo: Map[String, Any], data: QueryExecutionData, httpRequester: ActorRef) extends RunnablePipeline with Logging {

  private val config = ConfigFactory.load().getConfig("spark-data-lineage-listener.integrations.databricks.sql-statement-execution-api")

  private def getURI: String = {
    val hostname = config.getString("hostname")
    val port = config.getString("port")
    val endpoint = config.getString("api-url")

    s"$hostname:$port$endpoint"
  }

  private def buildPayload: String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd")

    val warehouseId = config.getString("warehouse-id")
    val table = config.getString("table")

    val queryId = "'" + data.queryId + "'"
    val executionId = data.executionId
    val timestamp = "'" + data.timestamp + "'"
    val trigger = "'" + data.trigger + "'"
    val duration = data.durationNs
    val status = "'" + data.status + "'"
    val failureReason = data.failureReason.js
    val jobs = data.jobIds.jc.replace("[", "array(").replace("]", ")")
    val stages = data.stageIds.jc.replace("[", "array(").replace("]", ")")
    val lineageData = "'" + data.data.jc.replace("'", "") + "'"
    val info = "'" + (integrationInfo - "apiToken").jc.replace("'", "") + "'"
    val dt = "'" + sdf.format(sdf.parse(data.timestamp)) + "'"

    val statement = s"""INSERT INTO $table VALUES ( $queryId, $executionId, $timestamp, $trigger, $duration, $status, $failureReason, $jobs, $stages, $lineageData, $info, $dt );"""

    Map(
      "statement" -> statement,
      "warehouse_id" -> warehouseId
    ).toJson.prettyPrint
  }

  override def run(): Unit = {
    val uri = getURI
    val payload = buildPayload
    val apiToken = integrationInfo("apiToken").toString

    log.info(s"Sending the following payload to $uri")
    log.info("\n" + payload)

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = uri,
      headers = immutable.Seq(
        Authorization(OAuth2BearerToken(apiToken))
      ),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        payload
      )
    )

    httpRequester ! request
  }
}
