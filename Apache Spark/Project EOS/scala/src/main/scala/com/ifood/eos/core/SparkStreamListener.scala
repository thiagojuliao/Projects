package com.ifood.eos.core

import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchClientBuilder}
import com.ifood.eos.utils.CloudWatchMetricsUtils.buildCloudwatchSlackStreamMessage
import com.ifood.eos.utils.SparkListenerUtils._
import com.ifood.eos.utils.SparkStreamListenerUtils._
import org.apache.spark.sql._
import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener._
import slack.api.SlackApiClient

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable

object SparkStreamListener {
  private[core] final val TOKEN = "xoxb-3379626902-1936046858291-BH2BjXu2N1z1ru7SHN9UDLol"

  private val sdf = new SimpleDateFormat("yyyy-MM-dd")
  private val spark = SparkSession.builder.getOrCreate
  private val info = {
  """
    [INFO]
    This stream listener will run in a background thread capturing every progress made from any active stream on this cluster.
    To only capture metrics from your own specific stream queries please use the method "addQueryNames(listOfQueryNames)" from your listener object.
    Bear in mind that is mandatory to set the query name on your DataFrameStreamWriter object for this strategy to work:
    +----------------------------------+
    +  spark.readStream                +
    +    ...                           +
    +    .writeStream                  +
    +    .queryName("xablau_is_back")  +
    +    ...                           +
    +----------------------------------+
    Only by doing this and passing the list of query names that you can isolate your runtime environment on a shared cluster.

    [IN MEMORY DATA MANAGEMENT]
    For real time reporting purposes we've considered saving all the gathered information on a in-memory buffer partitioned by date. Which means that every metrics gathered will reside on the same partition day, i.e., a key from a hash table.
    Every new day all previous data will be erased from memory after being sent as a report to the respective chosen Slack Channel.
    By doing this we'll avoid OOM errors specially on 24/7 streaming jobs.
  """.stripMargin
  }

  def getInfo: String = info

  def apply(jobName: String, product: String, slackChannelId: String, cloudWatchNamespace: String): SparkStreamListener = {
    val ssl = new SparkStreamListener(jobName, product, slackChannelId, cloudWatchNamespace, getNotebookURL)
    this.spark.streams.addListener(ssl)

    println(info)

    ssl
  }

  def apply(jobName: String, product: String, slackChannelId: String, cloudWatchNamespace: String, notebookURL: String): SparkStreamListener = {
    val ssl = new SparkStreamListener(jobName, product, slackChannelId, cloudWatchNamespace, notebookURL)
    spark.streams.addListener(ssl)

    println(info)

    ssl
  }
}

class SparkStreamListener(jobName: String, product: String, slackChannelId: String, cloudWatchNamespace: String, notebookURL: String) extends StreamingQueryListener with CloudWatchMetrics {
  import SparkStreamListener._

  require(jobName.nonEmpty)
  require(product.nonEmpty)
  require(slackChannelId.nonEmpty)
  require(cloudWatchNamespace.nonEmpty)
  require(notebookURL.nonEmpty)

  implicit val cw: AmazonCloudWatch = AmazonCloudWatchClientBuilder.defaultClient()
  implicit val cwNamespace: CloudWatchNamespace = cloudWatchNamespace

  private val slackClient = SlackApiClient(TOKEN)
  private val userQueries: mutable.HashMap[String, String] = mutable.HashMap()
  private val queryData: mutable.HashMap[String, Array[Row]] = mutable.HashMap()
  private var userQueryNames: Set[String] = Set()

  def addQueryNames(names: String*): Unit =
    this.userQueryNames = this.userQueryNames ++ names.toSet

  def addQueryNames(names: List[String]): Unit =
    this.userQueryNames = this.userQueryNames ++ names.toSet

  def addQueryNames(names: util.ArrayList[String]): Unit =
    this.userQueryNames = this.userQueryNames ++ names.toSet

  def getQueryData: DataFrame = buildQueryDF(this.queryData, sdf.format(new Date()))

  def getStreamMetrics: DataFrame = buildStreamMetrics(this.getQueryData)

  def clear(): Unit = {
    this.userQueryNames = Set()
    this.userQueries.clear()
    this.queryData.clear()

    spark.streams.removeListener(this)
  }

  override def onQueryStarted(queryStarted: QueryStartedEvent): Unit = {
    val queryId = queryStarted.id.toString
    val queryName = queryStarted.name

    if (!(this.userQueryNames contains queryName) && userQueryNames.nonEmpty) return
    if (!this.userQueries.contains(queryId)) this.userQueries.put(queryId, queryName)
  }

  override def onQueryProgress(queryProgress: QueryProgressEvent): Unit = {
    val progress = queryProgress.progress

    if (!this.userQueries.contains(progress.id.toString)) return

    val dt = sdf.format(new Date())
    val queryId = progress.id.toString
    val runId = progress.runId.toString
    val timestamp = progress.timestamp
    val name = progress.name
    val batchId = progress.batchId
    val batchDuration = progress.batchDuration
    val addBatchDuration = progress.durationMs.get("addBatch")
    val getBatchDuration = progress.durationMs.get("getBatch")
    val latestOffsetDuration = progress.durationMs.get("latestOffset")
    val queryPlanningDuration = progress.durationMs.get("queryPlanning")
    val triggerExecutionDuration = progress.durationMs.get("triggerExecution")
    val walCommitDuration = progress.durationMs.get("walCommit")
    val numInputRows = progress.numInputRows
    val numOutputRows = if (progress.sink.numOutputRows < 0) 0L else progress.sink.numOutputRows
    val inputRowsPerSecond = progress.inputRowsPerSecond.toLong
    val processedRowsPerSecond = progress.processedRowsPerSecond.toLong
    val datasetInfo = getDatasetInfo(name)

    val metadata = mutable.HashMap(
      "dt" -> dt,
      "query_id" -> queryId,
      "run_id" -> runId,
      "timestamp" -> timestamp,
      "query_name" -> name,
      "batch_id" -> batchId,
      "batch_duration" -> batchDuration,
      "add_batch_duration" -> addBatchDuration,
      "get_batch_duration" -> getBatchDuration,
      "latest_offset_duration" -> latestOffsetDuration,
      "query_planning_duration" -> queryPlanningDuration,
      "trigger_execution_duration" -> triggerExecutionDuration,
      "wal_commit_duration" -> walCommitDuration,
      "input_rows" -> numInputRows,
      "output_rows" -> numOutputRows,
      "input_rate" -> inputRowsPerSecond,
      "process_rate" -> processedRowsPerSecond,
      "job_name" -> jobName,
      "product" -> product,
      "zone" -> datasetInfo._1,
      "namespace" -> datasetInfo._2,
      "dataset" -> datasetInfo._3
    )

    // Atualiza os dados de métricas internamente na memória, no Datalake e CloudWatch
    try{
      // updateQueryData(this.queryData, metadata) (Disponível somente no release 2.0)
      updateStreamMetrics(metadata)
      this.pushToCloudWatch(this.slackClient, metadata)
    } catch {
      case e: Throwable => println(e)
    }
  }

  override def onQueryTerminated(queryTerminated: QueryTerminatedEvent): Unit = {
    val queryId = queryTerminated.id.toString
    val runId = queryTerminated.runId.toString
    val exception = queryTerminated.exception

    if (!this.userQueries.contains(queryId)) return

    val datasetInfo = getDatasetInfo(this.userQueries(queryId))

    if (exception.isDefined) {
      val metadata = mutable.HashMap(
        "job_name" -> jobName,
        "product" -> product,
        "zone" -> datasetInfo._1,
        "namespace" -> datasetInfo._2,
        "dataset" -> datasetInfo._3,
        "query_id" -> queryId,
        "run_id" -> runId,
        "exception" -> (exception.get.substring(1, 1000) + "...")
      )

      // Insere os dados na tabela de métricas no Datalake e envia o alerta no canal do Slack
      try {
        updateStreamFailMetrics(metadata)

        val message = buildSlackMessage(jobName, product, queryId, runId, datasetInfo._3, notebookURL)
        sendToSlack(this.slackClient, slackChannelId, message)
      } catch {
        case e: Throwable => println(e)
      }
    }
  }

  override def pushToCloudWatch(client: SlackApiClient, metadata: mutable.HashMap[String, Any]): Unit = {
    val jobName = metadata("job_name").toString
    val product = metadata("product").toString
    val namespace = metadata("namespace").toString
    val dataset = metadata("dataset").toString
    val queryId = metadata("query_id").toString
    val runId = metadata("run_id").toString
    val inputRows = metadata("input_rows").toString.toLong
    val processRate = metadata("process_rate").toString.toLong
    val batchDuration = metadata("batch_duration").toString.toLong

    val dimensions = mutable.Map(
        "dataset" -> dataset,
        "namespace" -> namespace,
        "job_name" -> jobName
      )

    try {
      pushCountMetric(dimensions, "input_rows", inputRows)
      pushCountMetric(dimensions, "process_rate", processRate)
      pushCountMetric(dimensions, "batch_duration", batchDuration)
    } catch {
      case e: Throwable =>
        sendToSlack(this.slackClient, slackChannelId, buildCloudwatchSlackStreamMessage(jobName, product, queryId, runId, notebookURL, e))
    }
  }
}
