package com.ifood.eos.core

import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchClientBuilder}
import com.ifood.eos.utils.CloudWatchMetricsUtils.buildCloudwatchSlackJobMessage
import com.ifood.eos.utils.SparkJobListenerUtils._
import com.ifood.eos.utils.SparkListenerUtils.{CloudWatchNamespace, getNotebookURL, sendToSlack}
import org.apache.spark.scheduler._
import org.apache.spark.sql._
import slack.api.SlackApiClient

import scala.collection.mutable

object SparkJobListener {
  private[core] final val TOKEN = "xoxb-3379626902-2227617355252-CXNuxVEreLCRXSR2Lhta29k1"

  private val info: String =
  """
    [INFO]
    To avoid imprecise results please set a watermark through "setWatermark(string)" method right before the command that you want to get a analysis from.
    Also add a remove watermark command through "removeWatermarks()" method right after the last command to avoid the listener to get any foreign information from your own background threads.
    Use the method "getJobMetrics()" from your listener object to retrieve the metrics dataframe.
    Keep in mind that some metrics are not yet formatted, therefore, metrics like shuffle reads/writes are still in bytes.
    If you want to retrieve the information from a specific watermarked step just add the watermark string as a parameter on the same method mentioned above.

    [GENERATING REPORTS]
    +-----------------+
    +   Scala Users   +
    +-----------------+
    Import "SparkJobReporter" object from package "com.ifood.eos.core" and use its static method "buildReport(metricsDataframe)" together with "displayHTML(html)" to display the report on a databricks notebook environment.

    +------------------+
    +   Python Users   +
    +------------------+
    Use the method "showReport(metricsDataframe)" on your listener object to automatically display the report on a databricks notebook environment.
  """.stripMargin

  def getInfo: String = info

  def apply(slackChannelId: String, cloudWatchNamespace: String): SparkJobListener = {
    val sjl = new SparkJobListener(getSessionInfo._1, getSessionInfo._2, slackChannelId, cloudWatchNamespace, getNotebookURL)

    sjl.setJobGroup()
    sjl.setOwnership()
    sjl.sc.addSparkListener(sjl)
    println(this.info)

    sjl
  }

  def apply(owner: String, jobName: String, slackChannelId: String, cloudWatchNamespace: String, notebookURL: String): SparkJobListener = {
    val sjl = new SparkJobListener(owner, jobName, slackChannelId, cloudWatchNamespace, notebookURL)

    sjl.setJobGroup()
    sjl.setOwnership()
    sjl.sc.addSparkListener(sjl)
    println(info)

    sjl
  }
}

class SparkJobListener(owner: String, jobName: String, slackChannelId: String, cloudWatchNamespace: String, notebookURL: String) extends SparkListener with CloudWatchMetrics {
  import SparkJobListener._

  require(owner.nonEmpty)
  require(jobName.nonEmpty)
  require(slackChannelId.nonEmpty)
  require(cloudWatchNamespace.nonEmpty)
  require(notebookURL.nonEmpty)

  implicit val cw: AmazonCloudWatch = AmazonCloudWatchClientBuilder.defaultClient()
  implicit val cwNamespace: CloudWatchNamespace = cloudWatchNamespace

  private val slackClient = SlackApiClient(TOKEN)
  private val spark = SparkSession.builder().getOrCreate()
  private val sc = this.spark.sparkContext
  private val sessionInfo = owner + " -> " + jobName

  private var isWatermarked = false
  private var userWatermarks: Set[String] = Set()
  private var userStageIds: Array[Int]  = Array()
  private var taskData: mutable.HashMap[String, Array[Row]] = mutable.HashMap()
  private var stageData: mutable.HashMap[String, Array[Row]] = mutable.HashMap()

  private def getTaskData: DataFrame = buildTaskDF(this.taskData)
  private def getStageData(watermark: String): DataFrame = buildStageDF(this.stageData, watermark)

  def setJobGroup(): Unit = this.sc.setJobGroup(jobName, "A job started from a Databricks notebook.")
  def setOwnership(): Unit = this.sc.setLocalProperty("callSite.short", this.sessionInfo)

  def setWatermark(s: String): Unit = {
    this.sc.setLocalProperty("callSite.long", s)
    this.isWatermarked = true
    this.userWatermarks = this.userWatermarks ++ Set(s)
  }

  def removeWatermarks(): Unit = {
    this.isWatermarked = false
    this.sc.clearCallSite()
    this.setOwnership()
    this.userWatermarks = Set()
  }

  def getJobMetrics(watermark: String = null): DataFrame = getMetrics(this.getTaskData, this.getStageData(watermark))

  def clear(): Unit = {
    this.sc.removeSparkListener(this)
    this.removeWatermarks()
    this.taskData = mutable.HashMap()
    this.stageData = mutable.HashMap()
  }

  override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = {
    val stageInfo = stageSubmitted.stageInfo
    val validWatermarks = this.userWatermarks.filter(stageInfo.details contains _)

    if (((stageInfo.name contains this.sessionInfo) || validWatermarks.nonEmpty) && this.isWatermarked)
      this.userStageIds = this.userStageIds ++ Array(stageInfo.stageId)
  }

  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    if (!(this.userStageIds contains taskEnd.stageId) || !this.isWatermarked) return

    val stageId = taskEnd.stageId
    val taskId =  taskEnd.taskInfo.taskId
    val completed = taskEnd.taskInfo.successful
    val taskDuration = taskEnd.taskInfo.duration
    val inputRecords = taskEnd.taskMetrics.inputMetrics.recordsRead
    val inputBytes = taskEnd.taskMetrics.inputMetrics.bytesRead
    val outputRecords = taskEnd.taskMetrics.outputMetrics.recordsWritten
    val outputBytes = taskEnd.taskMetrics.outputMetrics.bytesWritten
    val shuffleRead = taskEnd.taskMetrics.shuffleReadMetrics.localBytesRead + taskEnd.taskMetrics.shuffleReadMetrics.remoteBytesRead
    val shuffleWrite = taskEnd.taskMetrics.shuffleWriteMetrics.bytesWritten
    val memorySpill = taskEnd.taskMetrics.memoryBytesSpilled
    val diskSpill = taskEnd.taskMetrics.diskBytesSpilled
    val peakExecutionMemory = taskEnd.taskMetrics.peakExecutionMemory
    val GCTime = taskEnd.taskMetrics.jvmGCTime
    val errorMessage = getErrorMessageIfAny(taskEnd.reason)

    val metadata = mutable.HashMap(
      "stage_id" -> stageId,
      "task_id" -> taskId,
      "completed" -> completed,
      "task_duration" -> taskDuration,
      "input_records" -> inputRecords,
      "input_bytes" -> inputBytes,
      "output_records" -> outputRecords,
      "output_bytes" -> outputBytes,
      "shuffle_read" -> shuffleRead,
      "shuffle_write" -> shuffleWrite,
      "memory_spill" -> memorySpill,
      "disk_spill" -> diskSpill,
      "peak_execution_memory" -> peakExecutionMemory,
      "gc_time" -> GCTime,
      "failure_reason" -> errorMessage
    )

    try{
      updateTaskData(this.taskData, metadata)
      pushToCloudWatch(this.slackClient, metadata)
    } catch {
      case e: Throwable => println(e)
    }
  }

  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
    if (!(this.userStageIds contains stageCompleted.stageInfo.stageId) || !this.isWatermarked) return

    val stageId = stageCompleted.stageInfo.stageId
    val details = stageCompleted.stageInfo.details
    val duration = stageCompleted.stageInfo.completionTime.getOrElse(0L) - stageCompleted.stageInfo.submissionTime.getOrElse(0L)
    val numTasks = stageCompleted.stageInfo.numTasks
    val parentStageIds = stageCompleted.stageInfo.parentIds
    val failureReason = stageCompleted.stageInfo.failureReason

    val metadata = mutable.HashMap(
      "job_name" -> jobName,
      "stage_id" -> stageId,
      "name" -> owner,
      "details" -> details,
      "duration" -> duration,
      "num_tasks" -> numTasks,
      "parent_stage_ids" -> parentStageIds,
      "failure_reason" -> failureReason
    )

    try {
      updateStageData(this.stageData, metadata)
      this.userStageIds = this.userStageIds.filter(_ != stageId)
    } catch {
      case e: Throwable => println(e)
    }
  }

  override def pushToCloudWatch(client: SlackApiClient, metadata: mutable.HashMap[String, Any]): Unit = {
    val stageId = metadata("stage_id").toString
    val taskId = metadata("task_id").toString
    val completed = if (metadata("completed").toString.toBoolean) 1 else 0
    val taskDuration = metadata("task_duration").toString.toLong
    val inputRecords = metadata("input_records").toString.toLong
    val inputBytes = metadata("input_bytes").toString.toLong
    val outputRecords = metadata("output_records").toString.toLong
    val outputBytes = metadata("output_bytes").toString.toLong
    val shuffleRead = metadata("shuffle_read").toString.toLong
    val shuffleWrite = metadata("shuffle_write").toString.toLong
    val memorySpill = metadata("memory_spill").toString.toLong
    val peakExecutionMemory = metadata("peak_execution_memory").toString.toLong
    val GCTime = metadata("gc_time").toString.toLong
    val hasError = if (metadata("failure_reason") == null) 0 else 1

    val dimensions = mutable.Map(
        "job_name" -> jobName,
        "owner" -> owner,
        "stage_id" -> stageId,
        "task_id" -> taskId
      )

    try {
      pushCountMetric(dimensions, "completed", completed)
      pushCountMetric(dimensions, "has_error", hasError)
      pushCountMetric(dimensions, "task_duration", taskDuration)
      pushCountMetric(dimensions, "input_records", inputRecords)
      pushCountMetric(dimensions, "input_bytes", inputBytes)
      pushCountMetric(dimensions, "output_records", outputRecords)
      pushCountMetric(dimensions, "output_bytes", outputBytes)
      pushCountMetric(dimensions, "shuffle_read", shuffleRead)
      pushCountMetric(dimensions, "shuffle_write", shuffleWrite)
      pushCountMetric(dimensions, "memory_spill", memorySpill)
      pushCountMetric(dimensions, "peak_execution_memory", peakExecutionMemory)
      pushCountMetric(dimensions, "gc_time", GCTime)
    } catch {
      case e: Throwable =>
        sendToSlack(this.slackClient, slackChannelId, buildCloudwatchSlackJobMessage(jobName, owner, stageId, taskId, notebookURL, e))
    }
  }
}
