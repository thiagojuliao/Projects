package com.ifood.eos.utils

import akka.actor.ActorSystem
import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import org.apache.spark.sql.Column
import org.apache.spark.sql.catalyst.expressions.aggregate.ApproximatePercentile
import org.apache.spark.sql.functions.lit
import play.api.libs.json.Json
import slack.api.SlackApiClient

import scala.util.{Failure, Success, Try}

object SparkListenerUtils {
  type CloudWatchNamespace = String

  private implicit val system: ActorSystem = ActorSystem("SparkListenerUtils")
  private final val ICON_URL = Option("https://avatars.slack-edge.com/2021-04-07/1935850919346_1131a9896dac7acfd83c_512.jpg")
  private final val USERNAME = Option("Spark Listener Bot")

  // Função para extrair a URL do notebook em execução
  def getNotebookURL: String = {
    val notebookInfo = Json.parse(dbutils.notebook.getContext.toJson)
    val apiUrl: String = dbutils.notebook.getContext.apiUrl.get

    Try(notebookInfo("tags")("jobId")) match {
      case Failure(_) => apiUrl + "/" + notebookInfo("tags")("browserHash").toString.replace("\"", "")
      case Success(_) => (apiUrl + "/#job/" + notebookInfo("tags")("jobId").toString.replace("\"", "") + "/run/"
        + notebookInfo("tags")("idInJob").toString.replace("\"", ""))
    }
  }

  // Função para adquirir o nome do job quando não é passado via parâmetro
  def getJobName: String = {
    Json.parse(dbutils.notebook.getContext.toJson)("extraContext")("notebook_path").toString.split("/").reverse(0).replace("\"", "")
  }

  // Função para enviar a mensagem de erro para o canal do Slack
  def sendToSlack(client: SlackApiClient, channelId: String, message: String): Unit = {
    client.postChatMessage(channelId=channelId, text=message, username=USERNAME, iconUrl=ICON_URL)
  }

  // Função de cálculo de percentis
  // Anterior ao Spark 3.1. essa função precisa ser definida pois NÃO está pré-construída
  object PercentileApprox {

    def percentile_approx(col: Column, percentage: Column, accuracy: Column): Column = {
      val expr = new ApproximatePercentile(
        col.expr,  percentage.expr, accuracy.expr
      ).toAggregateExpression
      new Column(expr)
    }

    def percentile_approx(col: Column, percentage: Column): Column = percentile_approx(
      col, percentage, lit(ApproximatePercentile.DEFAULT_PERCENTILE_ACCURACY)
    )
  }
}
