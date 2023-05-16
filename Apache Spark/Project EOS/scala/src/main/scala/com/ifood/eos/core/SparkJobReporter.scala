package com.ifood.eos.core

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import com.ifood.eos.utils.SparkReporterUtils._

case object SparkJobReporter {
  private val spark = SparkSession.builder().getOrCreate()
  private var metricsData = this.spark.emptyDataFrame

  private val head =
    s"""
      <head>
        <style>
          body, html {
            height: 100%
          }
          h2 {
            color: blue
          }
          h3 {
            color: purple
          }
          .section-div {
            border-top: 10px outset
          }
          .sub-section-div {
            border-top: 3px outset
          }
          .metrics-data {
              border: 2px solid black;
              border-collapse: collapse;
              text-align: center
          }
        </style>
        <div style="background-color:white">
          <table align="center">
            <tr>
              <td><img src="$projectEOSImageSource" width="150px" height="150px"></td>
              <td><h1 style="color:orange"><b>Project EOS - Spark Job Reporter</b></h1></td>
            </tr>
          </table>
        </div>
        <div class="section-div"></div>
      </head>
  """

  private var body = "<body>"

  private def clearState(): Unit = {
    this.metricsData = spark.emptyDataFrame
    this.body = "<body>"
  }

  private def selectMetrics(metrics: DataFrame): DataFrame = {
    metrics
      .selectExpr(
        "job_name",
        "details",
        "stage_id",
        "total_tasks", // Task Failures Section
        "total_failed_tasks",
        "substr(cast(array_distinct(task_errors) as string), 1, size(task_errors) - 1) as task_errors",
        "total_duration", // Task Durations Section
        "min_task_duration",
        "task_duration_quantiles[0] as task_duration_p25",
        "task_duration_quantiles[1] as task_duration_p50",
        "task_duration_quantiles[2] as task_duration_p75",
        "max_task_duration",
        "total_shuffle_read", // Shuffle Metrics Section
        "min_shuffle_read",
        "shuffle_read_quantiles[0] as shuffle_read_p25",
        "shuffle_read_quantiles[1] as shuffle_read_p50",
        "shuffle_read_quantiles[2] as shuffle_read_p75",
        "max_shuffle_read",
        "total_shuffle_write",
        "min_shuffle_write",
        "shuffle_write_quantiles[0] as shuffle_write_p25",
        "shuffle_write_quantiles[1] as shuffle_write_p50",
        "shuffle_write_quantiles[2] as shuffle_write_p75",
        "max_shuffle_write",
        "total_memory_spill", // Memory Management Section
        "total_disk_spill",
        "min_peak_execution_memory",
        "peak_execution_memory_quantiles[0] as peak_execution_memory_p25",
        "peak_execution_memory_quantiles[1] as peak_execution_memory_p50",
        "peak_execution_memory_quantiles[2] as peak_execution_memory_p75",
        "max_peak_execution_memory",
        "min_gc_time",
        "gc_time_quantiles[0] as gc_time_p25",
        "gc_time_quantiles[1] as gc_time_p50",
        "gc_time_quantiles[2] as gc_time_p75",
        "max_gc_time"
      )
  }

  private def addJobNameSection(metrics: DataFrame): Unit = {
    val jobName = metrics.select("job_name").dropDuplicates().collect()(0)(0).toString

    this.body = this.body +
      s"""
      <br>
      ${printTableTitle(jobName, 30, "black", sparkJobImageSource)}
      <br>
      <div class="sub-section-div"></div>
    """
  }

  private def addTaskFailuresSection(step: String): Unit = {
    val sectionData = this.metricsData
      .filter(col("details") === step)
      .select(
        "stage_id",
        "total_tasks",
        "total_failed_tasks",
        "task_errors"
      ).collect()

    val table =
      s"""
      ${printTableTitle("Task Failures", 20, "purple", taskFailureImageSource)}
      <table align="center" class="metrics-data">
        <tr class="metrics-data">
          <th class="metrics-data">Stage Id</th>
          <th class="metrics-data">Total Tasks</th>
          <th class="metrics-data">Total Failed Tasks</th>
          <th class="metrics-data">Task Errors</th>
        </tr>
        ${printRows(sectionData)}
      </table><br>
    """

    this.body = this.body + table
  }

  private def addTaskDurationsSection(step: String): Unit = {
    val sectionData = this.metricsData
      .filter(col("details") === step)
      .select(
        "stage_id",
        "total_duration",
        "min_task_duration",
        "task_duration_p25",
        "task_duration_p50",
        "task_duration_p75",
        "max_task_duration"
      ).collect().map(
      r => Row(
        r(0), formatDuration(r(1).toString.toLong), formatDuration(r(2).toString.toLong), formatDuration(r(3).toString.toLong),
        formatDuration(r(4).toString.toLong), formatDuration(r(5).toString.toLong), formatDuration(r(6).toString.toLong)
      )
    )

    val table =
      s"""
      ${printTableTitle("Task Durations", 20, "purple", taskDurationImageSource)}
      <table align="center" class="metrics-data">
        <tr class="metrics-data">
          <th class="metrics-data">Stage Id</th>
          <th class="metrics-data">Total Duration</th>
          <th class="metrics-data">Min</th>
          <th class="metrics-data">25th Percentile</th>
          <th class="metrics-data">Median</th>
          <th class="metrics-data">75th Percentile</th>
          <th class="metrics-data">Max</th>
        </tr>
        ${printRows(sectionData)}
      </table><br>
    """

    this.body = this.body + table
  }

  private def addShuffleMetricsSection(step: String): Unit = {
    val sectionData = this.metricsData
      .filter(col("details") === step)
      .select(
        "stage_id",
        "total_shuffle_read",
        "min_shuffle_read",
        "shuffle_read_p25",
        "shuffle_read_p50",
        "shuffle_read_p75",
        "max_shuffle_read",
        "total_shuffle_write",
        "min_shuffle_write",
        "shuffle_write_p25",
        "shuffle_write_p50",
        "shuffle_write_p75",
        "max_shuffle_write"
      ).collect().map(
      r => Row(
        r(0), bytesToString(r(1).toString.toLong), bytesToString(r(2).toString.toLong), bytesToString(r(3).toString.toLong),
        bytesToString(r(4).toString.toLong), bytesToString(r(5).toString.toLong), bytesToString(r(6).toString.toLong),
        bytesToString(r(7).toString.toLong), bytesToString(r(8).toString.toLong), bytesToString(r(9).toString.toLong),
        bytesToString(r(10).toString.toLong), bytesToString(r(11).toString.toLong), bytesToString(r(12).toString.toLong)
      )
    )

    val table =
      s"""
      ${printTableTitle("Shuffle Metrics", 20, "purple", shuffleMetricsImageSource)}
      <table align="center" class="metrics-data">
        <tr class="metrics-data">
          <th class="metrics-data">Stage Id</th>
          <th class="metrics-data">Shuffle Read Size</th>
          <th class="metrics-data">Min</th>
          <th class="metrics-data">25th Percentile</th>
          <th class="metrics-data">Median</th>
          <th class="metrics-data">75th Percentile</th>
          <th class="metrics-data">Max</th>
          <th class="metrics-data">Shuffle Write Size</th>
          <th class="metrics-data">Min</th>
          <th class="metrics-data">25th Percentile</th>
          <th class="metrics-data">Median</th>
          <th class="metrics-data">75th Percentile</th>
          <th class="metrics-data">Max</th>
        </tr>
        ${printRows(sectionData)}
      </table><br>
    """

    this.body = this.body + table
  }

  private def addMemoryManagementSection(step: String): Unit = {
    val sectionData = this.metricsData
      .filter(col("details") === step)
      .select(
        "stage_id",
        "total_memory_spill",
        "total_disk_spill",
        "min_peak_execution_memory",
        "peak_execution_memory_p25",
        "peak_execution_memory_p50",
        "peak_execution_memory_p75",
        "max_peak_execution_memory",
        "min_gc_time",
        "gc_time_p25",
        "gc_time_p50",
        "gc_time_p75",
        "max_gc_time"
      ).collect().map(
      r => Row(
        r(0), bytesToString(r(1).toString.toLong), bytesToString(r(2).toString.toLong), bytesToString(r(3).toString.toLong),
        bytesToString(r(4).toString.toLong), bytesToString(r(5).toString.toLong), bytesToString(r(6).toString.toLong),
        bytesToString(r(7).toString.toLong), formatDuration(r(8).toString.toLong), formatDuration(r(9).toString.toLong),
        formatDuration(r(10).toString.toLong), formatDuration(r(11).toString.toLong), formatDuration(r(12).toString.toLong)
      )
    )

    val table =
      s"""
      ${printTableTitle("Memory Management", 20, "purple", memoryManagementImageSource)}
      <table align="center" class="metrics-data">
        <tr class="metrics-data">
          <th class="metrics-data">Stage Id</th>
          <th class="metrics-data">Memory Spill Size</th>
          <th class="metrics-data">Disk Spill Size</th>
          <th class="metrics-data">Peak Execution Memory Min</th>
          <th class="metrics-data">Peak Execution Memory 25th Percentile</th>
          <th class="metrics-data">Peak Execution Memory Median</th>
          <th class="metrics-data">Peak Execution Memory 75th Percentile</th>
          <th class="metrics-data">Peak Execution Memory Max</th>
          <th class="metrics-data">GC Time Min</th>
          <th class="metrics-data">GC Time 25th Percentile</th>
          <th class="metrics-data">GC Time Median</th>
          <th class="metrics-data">GC Time 75th Percentile</th>
          <th class="metrics-data">GC Time Max</th>
        </tr>
        ${printRows(sectionData)}
      </table><br>
    """

    this.body = this.body + table
  }

  private def addPerformanceTipsSection(): String = {
    s"""
      ${printTableTitle("Performance", 25, "green", tipsImageSource, 150)}
      <p> • <b style="color:green"><i>Tip 01</b></i>: If the <b>maximum task duration</b> is greatly higher than both the <b>median</b> and the <b>75th percentile</b>, your job is probably having some <b>data skewness</b>. Large size partitions implies on a larger task duration which often causes <b>OOM</b> errors and <b>Disk Spilling</b>, hence heavily downgrading your job performance. Increasing the value of <b>spark.sql.shuffle.partitions</b> and/or applying <b>salting</b> might help you with this problem.<p>
      <p> • <b style="color:green"><i>Tip 02</b></i>: <b>Shuffle metrics</b> are a crucial factor to help you investigate problems with job performance. A higher value on <b>shuffle reads</b> implies large partition size which often leads to <b>Disk Spilling</b>. Applying <b>salting</b>, <b>repartition</b> and/or increasing the value of <b>spark.sql.shuffle.partitions</b> might help you reduce shuffle read sizes.</p>
      <p> • <b style="color:green"><i>Tip 03</b></i>: When working with large data structures <b>Garbage Collector</b> might become a issue on your job applications. Bear in mind that spark runs on a <b>JVM (Java Virtual Machine)</b> container and has a memory management tool to clear old objects in memory that are not being used. Too much garbage collecting causes <b>CPU pressure</b> and often increasing the time to finish your tasks (too much time spending cleaning garbage). To minimize this problem try looking for expensive memory operations that tend to use large size objects, like <b>UDFs</b> that searches a bigger array for example, and try to think on other ways to solve the same problem (more simple the better). You can change the <b>GC</b> from your cluster by passing a <b>spark-conf</b> argument on a airflow yaml under the <b>cluster-config</b> key like: <b>spark.executor.extraJavaOptions -XX:+UseG1GC</b>.<p>
      <div class="sub-section-div"></div>
    """
  }

  private def buildSections(step: String): Unit = {
    this.body = this.body + s"""<div><h2 align="center"><u><i>$step</i></u></h2></div><br>"""

    this.addTaskFailuresSection(step)
    this.addTaskDurationsSection(step)
    this.addShuffleMetricsSection(step)
    this.addMemoryManagementSection(step)

    this.body = this.body + """<div class="sub-section-div"></div>"""
  }

  def buildReport(metrics: DataFrame, tipsEnabled: Boolean = true): String = {
    this.metricsData = this.selectMetrics(metrics)

    // Adiciona a secção contendo o nome do job
    this.addJobNameSection(this.metricsData)

    // Constrói o restante das secções para cada step dentro do job
    val steps = this.metricsData
      .select("stage_id", "details")
      .dropDuplicates(Array("details")).collect()
      .sortWith((r1, r2) => r1.getInt(0) < r2.getInt(0))
      .map(_(1).toString)

    steps.foreach(this.buildSections)

    // Unifica as partes e monta o documento final
    val html = {
      if (tipsEnabled) this.head + "\n" + this.body + "\n" + this.addPerformanceTipsSection() + "\n" + addFooterSection() + "\n</body>"
      else this.head + "\n" + this.body + addFooterSection() + "\n</body>"
    }

    // Retorna ao estado inicial
    this.clearState()

    html
  }
}
