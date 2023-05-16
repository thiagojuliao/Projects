package com.ifood.eos.utils

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import scala.collection.mutable
import scala.collection.JavaConverters.seqAsJavaListConverter

import SparkListenerUtils._

object SparkStreamListenerUtils {
  final val METRICS_TABLE = "pipeline_governance_raw.stream_metrics"
  final val FAIL_METRICS_TABLE = "pipeline_governance_raw.stream_fail_metrics"

  private val spark = SparkSession.builder.getOrCreate()
  private val sc = spark.sparkContext

  // Função para extrair as informações do dataset a partir do query name
  def getDatasetInfo(queryName: String): (String, String, String) = {
    val datasetInfoPattern = raw"(\w+)_(transient|raw|curated|sandbox|bronze|silver|gold).([A-z-]+)".r

    val infos = queryName match {
      case datasetInfoPattern(namespace, zone, dataset) => (zone, namespace, dataset.replace("-", "_"))
      case _ => ("???", "???", "???")
    }

    infos
  }

  // Função que atualiza as métricas de stream internamente na memória
  def updateQueryData(queryData: mutable.HashMap[String, Array[Row]], metadata: mutable.HashMap[String, Any]): Unit = {
    val dt = metadata("dt").toString
    val queryId = metadata("query_id").toString
    val runId = metadata("run_id").toString
    val timestamp = metadata("timestamp").toString
    val queryName = metadata("query_name").toString
    val batchId = metadata("batch_id").toString.toLong
    val batchDuration = metadata("batch_duration").toString.toLong
    val addBatchDuration = metadata("add_batch_duration").toString.toLong
    val getBatchDuration = metadata("get_batch_duration").toString.toLong
    val latestOffsetDuration = metadata("latest_offset_duration").toString.toLong
    val queryPlanningDuration = metadata("query_planning_duration").toString.toLong
    val triggerExecutionDuration = metadata("trigger_execution_duration").toString.toLong
    val walCommitDuration = metadata("wal_commit_duration").toString.toLong
    val inputRows = metadata("input_rows").toString.toLong
    val outputRows = metadata("output_rows").toString.toLong
    val inputRate = metadata("input_rate").toString.toLong
    val processRate = metadata("process_rate").toString.toLong
    val jobName = metadata("job_name").toString
    val product = metadata("product").toString
    val zone = metadata("zone").toString
    val namespace = metadata("namespace").toString
    val dataset = metadata("dataset").toString

    val oldData = queryData.getOrElse(dt, Array())
    val newData = Array(Row(
      queryId, runId, timestamp, queryName, batchId, batchDuration, addBatchDuration, getBatchDuration, latestOffsetDuration, queryPlanningDuration,
      triggerExecutionDuration, walCommitDuration, inputRows, outputRows, inputRate, processRate, jobName, product, zone, namespace, dataset
    ))

    if (!queryData.contains(dt)) queryData.put(dt, newData)
    else queryData.update(dt, oldData ++ newData)
  }

  // Função que insere dados na tabela de métricas no Datalake
  def updateStreamMetrics(metadata: mutable.HashMap[String, Any]): Unit = {
    val queryId = metadata("query_id").toString
    val runId = metadata("run_id").toString
    val timestamp = metadata("timestamp").toString
    val jobName = metadata("job_name").toString
    val product = metadata("product").toString
    val zone = metadata("zone").toString
    val namespace = metadata("namespace").toString
    val dataset = metadata("dataset").toString
    val batchId = metadata("batch_id").toString.toLong
    val batchDuration = metadata("batch_duration").toString.toLong
    val inputRows = metadata("input_rows").toString.toLong
    val inputRate = metadata("input_rate").toString.toLong
    val processRate = metadata("process_rate").toString.toLong

    // Cria a tabela caso ela não exista
    val createTableSQL =
      s"""
    CREATE TABLE IF NOT EXISTS $METRICS_TABLE
    (
      job_name STRING,
      query_id STRING,
      run_id STRING,
      timestamp STRING,
      product STRING,
      zone STRING,
      namespace STRING,
      dataset STRING,
      batch_id BIGINT,
      batch_duration BIGINT,
      input_rows BIGINT,
      input_rate BIGINT,
      process_rate BIGINT,
      total_cores BIGINT,
      dt STRING
    )
    USING PARQUET
    PARTITIONED BY (product, zone, namespace, dataset, dt)
  """
    this.spark.sql(createTableSQL)

    // Insere os dados na tabela de métricas
    val row = List(Row(jobName, queryId, runId, timestamp, product, zone, namespace, dataset, batchId, batchDuration,
      inputRows, inputRate, processRate)).asJava
    val schema = StructType(
      List(
        StructField("job_name", StringType),
        StructField("query_id", StringType),
        StructField("run_id", StringType),
        StructField("timestamp", StringType),
        StructField("product", StringType),
        StructField("zone", StringType),
        StructField("namespace", StringType),
        StructField("dataset", StringType),
        StructField("batch_id", LongType),
        StructField("batch_duration", LongType),
        StructField("input_rows", LongType),
        StructField("input_rate", LongType),
        StructField("process_rate", LongType)
      )
    )

    val df = this.spark.createDataFrame(row, schema)
      .withColumn("total_cores", lit(this.sc.defaultParallelism))
      .withColumn("dt", date_format(col("timestamp"), "yyyy-MM-dd"))

    df.write
      .format("parquet")
      .mode("append")
      .partitionBy("product", "zone", "namespace", "dataset", "dt")
      .saveAsTable(METRICS_TABLE)
  }

  // Função para inserção das métricas de falha no datalake
  def updateStreamFailMetrics(metadata: mutable.HashMap[String, String]): Unit = {
    val jobName = metadata("job_name")
    val product = metadata("product")
    val zone = metadata("zone")
    val namespace = metadata("namespace")
    val dataset = metadata("dataset")
    val queryId = metadata("query_id")
    val runId = metadata("run_id")
    val exception = metadata("exception")

    // Cria a tabela caso ela não exista
    val createTableSQL =
      s"""
    CREATE TABLE IF NOT EXISTS $FAIL_METRICS_TABLE
    (
      job_name STRING,
      product STRING,
      zone STRING,
      namespace STRING,
      dataset STRING,
      query_id STRING,
      run_id STRING,
      exception STRING,
      timestamp STRING,
      dt STRING
    )
    USING PARQUET
    PARTITIONED BY (product, zone, namespace, dataset, dt)
  """
    this.spark.sql(createTableSQL)

    // Insere os dados na tabela de métricas
    val row = List(Row(jobName, product, zone, namespace, dataset, queryId, runId, exception)).asJava
    val schema = StructType(
      List(
        StructField("job_name", StringType),
        StructField("product", StringType),
        StructField("zone", StringType),
        StructField("namespace", StringType),
        StructField("dataset", StringType),
        StructField("query_id", StringType),
        StructField("run_id", StringType),
        StructField("exception", StringType)
      )
    )

    val df = this.spark.createDataFrame(row, schema)
      .withColumn("timestamp", current_timestamp().cast(StringType))
      .withColumn("dt", date_format(current_date(), "yyyy-MM-dd"))

    df.write
      .format("parquet")
      .mode("append")
      .partitionBy("product", "zone", "namespace", "dataset", "dt")
      .saveAsTable(FAIL_METRICS_TABLE)
  }

  // Função para compor a mensagem de erro a ser enviada no Slack
  def buildSlackMessage(jobName: String, product: String, queryId: String, runId: String, dataset: String, notebookUrl: String): String = {
    // Modelo de mensagem a ser enviado em caso de erro
    s"""
<!channel>
:ahhhhhhhhh: *Stream Failure Alert* :ahhhhhhhhh:

*Job Name*: $jobName
*Product*: $product
*Query ID*: $queryId
*Run ID*: $runId
*Dataset*: $dataset
*Notebook URL*: `$notebookUrl`

For more details please run the following query:
```
%SQL
SELECT
  job_name,
  product,
  dataset,
  query_id,
  run_id,
  exception
FROM
  $FAIL_METRICS_TABLE
WHERE
  product = "$product" AND dataset = "$dataset" AND
  job_name = "$jobName" AND dt = current_date() AND
  query_id = "$queryId" AND run_id = "$runId"
```
  """.stripMargin
  }

  // Função para construção do DataFrame de Métricas
  def buildQueryDF(queryData: mutable.HashMap[String, Array[Row]], dt: String): DataFrame = {
    val schema = StructType(
      List(
        StructField("query_id", StringType),
        StructField("run_id", StringType),
        StructField("timestamp", StringType),
        StructField("query_name", StringType),
        StructField("batch_id", LongType),
        StructField("batch_duration", LongType),
        StructField("add_batch_duration", LongType),
        StructField("get_batch_duration", LongType),
        StructField("latest_offset_duration", LongType),
        StructField("query_planning_duration", LongType),
        StructField("trigger_execution_duration", LongType),
        StructField("wal_commit_duration", LongType),
        StructField("input_rows", LongType),
        StructField("output_rows", LongType),
        StructField("input_rate", LongType),
        StructField("process_rate", LongType),
        StructField("job_name", StringType),
        StructField("product", StringType),
        StructField("zone", StringType),
        StructField("namespace", StringType),
        StructField("dataset", StringType)
      )
    )

    val rows = queryData(dt).toList.asJava

    this.spark.createDataFrame(rows, schema)
  }

  // Função para extração das métricas de streaming
  def buildStreamMetrics(metrics: DataFrame): DataFrame = {
    import PercentileApprox._

    // Métricas que construiremos nesse dataset:
    /*
      Total Batches
      total, min, p25, p50, p75, max Batch Duration
      total, min, p25, p50, p75, max Add Batch Duration
      total, min, p25, p50, p75, max Get Batch Duration
      total, min, p25, p50, p75, max Latest Offset Duration
      total, min, p25, p50, p75, max Query Planning Duration
      total, min, p25, p50, p75, max Trigger Execution Duration
      total, min, p25, p50, p75, max Wal Commit Duration
      total, min, p25, p50, p75, max Input Rows
      total, min, p25, p50, p75, max Output Rows
      total, min, p25, p50, p75, max Input Rate
      total, min, p25, p50, p75, max Processing Rate
    */

    metrics
      .groupBy("job_name", "query_name")
      .agg(
        //+------------------------------------------ Query Name Section -------------------------------------------------------------+
        countDistinct("batch_id").alias("total_batches"),
        //+------------------------------------------ Batch Durations Section --------------------------------------------------------+
        sum("batch_duration").alias("total_batch_duration"),
        min("batch_duration").alias("min_batch_duration"),
        percentile_approx(col("batch_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("batch_duration_quantiles"),
        max("batch_duration").alias("max_batch_duration"),
        sum("add_batch_duration").alias("total_add_batch_duration"),
        min("add_batch_duration").alias("min_add_batch_duration"),
        percentile_approx(col("add_batch_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("add_batch_duration_quantiles"),
        max("add_batch_duration").alias("max_add_batch_duration"),
        sum("get_batch_duration").alias("total_get_batch_duration"),
        min("get_batch_duration").alias("min_get_batch_duration"),
        percentile_approx(col("get_batch_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("get_batch_duration_quantiles"),
        max("get_batch_duration").alias("max_get_batch_duration"),
        sum("latest_offset_duration").alias("total_latest_offset_duration"),
        min("latest_offset_duration").alias("min_latest_offset_duration"),
        percentile_approx(col("latest_offset_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("latest_offset_duration_quantiles"),
        max("latest_offset_duration").alias("max_latest_offset_duration"),
        sum("query_planning_duration").alias("total_query_planning_duration"),
        min("query_planning_duration").alias("min_query_planning_duration"),
        percentile_approx(col("query_planning_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("query_planning_duration_quantiles"),
        max("query_planning_duration").alias("max_query_planning_duration"),
        sum("trigger_execution_duration").alias("total_trigger_execution_duration"),
        min("trigger_execution_duration").alias("min_trigger_execution_duration"),
        percentile_approx(col("trigger_execution_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("trigger_execution_duration_quantiles"),
        max("trigger_execution_duration").alias("max_trigger_execution_duration"),
        sum("wal_commit_duration").alias("total_wal_commit_duration"),
        min("wal_commit_duration").alias("min_wal_commit_duration"),
        percentile_approx(col("wal_commit_duration"), array(lit(0.25), lit(0.5), lit(0.75))).alias("wal_commit_duration_quantiles"),
        max("wal_commit_duration").alias("max_wal_commit_duration"),
        //+------------------------------------------ Inbound Metrics Section --------------------------------------------------------+
        sum("input_rows").alias("total_input_rows"),
        min("input_rows").alias("min_input_rows"),
        percentile_approx(col("input_rows"), array(lit(0.25), lit(0.5), lit(0.75))).alias("input_rows_quantiles"),
        max("input_rows").alias("max_input_rows"),
        sum("input_rate").alias("total_input_rate"),
        min("input_rate").alias("min_input_rate"),
        percentile_approx(col("input_rate"), array(lit(0.25), lit(0.5), lit(0.75))).alias("input_rate_quantiles"),
        max("input_rate").alias("max_input_rate"),
        //+------------------------------------------ Outbound Metrics Section --------------------------------------------------------+
        sum("output_rows").alias("total_output_rows"),
        min("output_rows").alias("min_output_rows"),
        percentile_approx(col("output_rows"), array(lit(0.25), lit(0.5), lit(0.75))).alias("output_rows_quantiles"),
        max("output_rows").alias("max_output_rows"),
        sum("process_rate").alias("total_process_rate"),
        min("process_rate").alias("min_process_rate"),
        percentile_approx(col("process_rate"), array(lit(0.25), lit(0.5), lit(0.75))).alias("process_rate_quantiles"),
        max("process_rate").alias("max_process_rate")
      )
  }
}
