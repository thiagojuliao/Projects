package com.ifood.eos.utils

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import SparkListenerUtils.PercentileApprox._
import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable

object SparkJobListenerUtils {
  private val spark: SparkSession = SparkSession.builder().getOrCreate()
  private val sdf = new SimpleDateFormat("yyyy-MM-dd")
  private val dt = sdf.format(new Date())


  // Função para a atualização dos dados de métricas das tasks
  def updateTaskData(taskData: mutable.HashMap[String, Array[Row]], metadata: mutable.HashMap[String, Any]): Unit = {
    val stageId = metadata.getOrElse("stage_id", null)
    val taskId =  metadata.getOrElse("task_id", null)
    val completed = metadata.getOrElse("completed", null)
    val taskDuration = metadata.getOrElse("task_duration", null)
    val inputRecords = metadata.getOrElse("input_records", null)
    val inputBytes = metadata.getOrElse("input_bytes", null)
    val outputRecords = metadata.getOrElse("output_records", null)
    val outputBytes = metadata.getOrElse("output_bytes", null)
    val shuffleRead = metadata.getOrElse("shuffle_read", null)
    val shuffleWrite = metadata.getOrElse("shuffle_write", null)
    val memorySpill = metadata.getOrElse("memory_spill", null)
    val diskSpill = metadata.getOrElse("disk_spill", null)
    val peakExecutionMemory = metadata.getOrElse("peak_execution_memory", null)
    val GCTime = metadata.getOrElse("gc_time", null)
    val errorMessage = metadata.getOrElse("failure_reason", null)

    val oldData: Array[Row] = taskData.getOrElse(stageId.toString, Array())
    val newData = Array(Row(stageId, taskId, completed, taskDuration, inputRecords, inputBytes, outputRecords, outputBytes, shuffleRead,
      shuffleWrite, memorySpill, diskSpill, peakExecutionMemory, GCTime, errorMessage))

    if (oldData.isEmpty) taskData.put(stageId.toString, newData)
    else taskData.update(stageId.toString, oldData ++ newData)
  }

  // Função para atualização dos dados de métricas dos estágios
  def updateStageData(stageData: mutable.HashMap[String, Array[Row]], metadata: mutable.HashMap[String, Any]): Unit = {
    val jobName = metadata.getOrElse("job_name", null)
    val name = metadata.getOrElse("name", null)
    val stageId = metadata.getOrElse("stage_id", null)
    val details = metadata.getOrElse("details", null)
    val duration = metadata.getOrElse("duration", null)
    val numTasks = metadata.getOrElse("num_tasks", null)
    val parentStageIds = metadata.getOrElse("parent_stage_ids", null)
    val failureReason = metadata.getOrElse("failure_reason", null)

    val oldData: Array[Row] = stageData.getOrElse(stageId.toString, Array())
    val newData = Array(Row(jobName, name, stageId, details, duration, numTasks, parentStageIds, failureReason, dt))

    if (oldData.isEmpty) stageData.put(stageId.toString, newData)
    else stageData.update(stageId.toString, oldData ++ newData)
  }

  // Função para modelagem do objeto reason afim de extrair a mensagem de erro se existir alguma
  def getErrorMessageIfAny(reason: TaskEndReason): String = {
    reason match {
      case r: ExceptionFailure => r.toErrorString
      case r: ExecutorLostFailure => r.toErrorString
      case r: FetchFailed => r.toErrorString
      case r: TaskCommitDenied => r.toErrorString
      case r: TaskKilled => r.toErrorString
      case _ => null
    }
  }

  // Função que retorna um dataframe contendo as informações das métricas de cada task
  def buildTaskDF(taskData: mutable.HashMap[String, Array[Row]]): DataFrame = {
    val schema = StructType(
      List(
        StructField("stage_id", IntegerType),
        StructField("task_id", LongType),
        StructField("completed", BooleanType),
        StructField("task_duration", LongType),
        StructField("input_records", LongType),
        StructField("input_bytes", LongType),
        StructField("output_records", LongType),
        StructField("output_bytes", LongType),
        StructField("shuffle_read", LongType),
        StructField("shuffle_write", LongType),
        StructField("memory_spill", LongType),
        StructField("disk_spill", LongType),
        StructField("peak_execution_memory", LongType),
        StructField("gc_time", LongType),
        StructField("task_failure_reason", StringType)
      )
    )

    val rows = taskData.values.reduce(_ ++ _).toList.asJava
    spark.createDataFrame(rows, schema)
  }

  // Função que retorna um dataframe contendo as informações das métricas de cada estágio
  def buildStageDF(stageData: mutable.HashMap[String, Array[Row]], watermark: String): DataFrame = {
    val schema = StructType(
      List(
        StructField("job_name", StringType),
        StructField("user", StringType),
        StructField("stage_id", IntegerType),
        StructField("details", StringType),
        StructField("duration", LongType),
        StructField("num_tasks", IntegerType),
        StructField("parent_stage_ids", ArrayType(IntegerType)),
        StructField("failure_reason", StringType),
        StructField("dt", StringType)
      )
    )

    val rows = stageData.values.reduce(_ ++ _).toList.asJava

    if (Option(watermark).isDefined) spark.createDataFrame(rows, schema).filter(col("details").contains(watermark))
    else spark.createDataFrame(rows, schema)
  }

  // Função para construir o dataset contendo as informações agregadas de cada estágio
  def getMetrics(tasksDF: DataFrame, stagesDF: DataFrame): DataFrame = {
    // Métricas que construiremos nesse dataset:
    /*
      Total Duration
      Total Input Records
      Total Input Bytes
      Total Output Records
      Total Output Bytes
      Total Shuffle Read
      Total Shuffle Write
      Total Memory Spill
      Total Disk Spill
      min, p25, p50, p75, max para Duration, GC Time, Shuffle Read, Shuffle Write, Peak Execution Memory
      Total Failed Tasks
      Set of Task Errors Found
    */

    stagesDF.join(tasksDF, usingColumns=Seq("stage_id"))
      .groupBy("job_name", "user", "stage_id", "details", "parent_stage_ids", "failure_reason", "dt")
      .agg(
        max("duration").alias("total_duration"),
        max("num_tasks").alias("total_tasks"),
        sum(expr("case when not completed then 1 else 0 end")).alias("total_failed_tasks"),
        collect_set("task_failure_reason").alias("task_errors"),
        sum("input_records").alias("total_input_records"),
        sum("input_bytes").alias("total_input_bytes"),
        sum("output_records").alias("total_output_records"),
        sum("output_bytes").alias("total_output_bytes"),
        sum("shuffle_read").alias("total_shuffle_read"),
        sum("shuffle_write").alias("total_shuffle_write"),
        sum("memory_spill").alias("total_memory_spill"),
        sum("disk_spill").alias("total_disk_spill"),
        min("task_duration").alias("min_task_duration"),
        percentile_approx(col("task_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("task_duration_quantiles"),
        max("task_duration").alias("max_task_duration"),
        min("gc_time").alias("min_gc_time"),
        percentile_approx(col("gc_time"), array(lit(0.25), lit(0.5), lit(0.75))).alias("gc_time_quantiles"),
        max("gc_time").alias("max_gc_time"),
        min("shuffle_read").alias("min_shuffle_read"),
        percentile_approx(col("shuffle_read"), array(lit(0.25), lit(0.5), lit(0.75))).alias("shuffle_read_quantiles"),
        max("shuffle_read").alias("max_shuffle_read"),
        min("shuffle_write").alias("min_shuffle_write"),
        percentile_approx(col("shuffle_write"), array(lit(0.25), lit(0.5), lit(0.75))).alias("shuffle_write_quantiles"),
        max("shuffle_write").alias("max_shuffle_write"),
        min("peak_execution_memory").alias("min_peak_execution_memory"),
        percentile_approx(col("peak_execution_memory"), array(lit(0.25), lit(0.5), lit(0.75))).alias("peak_execution_memory_quantiles"),
        max("peak_execution_memory").alias("max_peak_execution_memory")
      )
      .orderBy("job_name", "stage_id")
  }

  // Função para extração do usuário e o nome da aplicação Spark (Não funciona no python!!!!)
  def getSessionInfo: (String, String) = {
    val prodPathRegex = raw"/prd/etl/(\w+)/(\w+)/(\w+)".r
    val devPathRegex = raw"/Users/(\w+\.\w+@ifood.com.br)?.+/(.+)".r

    val notebookPath = dbutils.notebook.getContext.notebookPath.getOrElse("")

    notebookPath match {
      case prodPathRegex(zone, namespace, dataset) => ("iFood Prod", s"${namespace}_$zone.$dataset")
      case devPathRegex(user, notebook) => (user, notebook)
      case _ => ("Unknown", "Unknown")
    }
  }
}
