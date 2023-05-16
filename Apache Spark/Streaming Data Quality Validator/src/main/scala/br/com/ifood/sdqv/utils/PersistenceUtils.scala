package br.com.ifood.sdqv.utils

import br.com.ifood.sdqv.core.GeneralTypes._
import br.com.ifood.sdqv.utils.StreamingDataQualityValidatorUtils.createMetricsDataFrame

import org.apache.spark.sql.{DataFrame, SparkSession}

case object PersistenceUtils {
  /**
   * Name of the metrics table to write the metrics into
   */
  val METRICS_TABLE: String = "generic_sandbox.data_quality_metrics"

  /**
   * Creates the metrics table if not exists
   * @param spark A Spark session object implicitly given
   */
  def createMetricsTableIfNotExists()(implicit spark: SparkSession): Unit = {
    val createTableSQL: String =
      s"""
        CREATE TABLE IF NOT EXISTS $METRICS_TABLE
        (
          process_name STRING,
          type STRING,
          mode STRING,
          zone_stage STRING,
          namespace_product STRING,
          dataset STRING,
          batch_id BIGINT,
          timestamp STRING,
          column STRING,
          metric STRING,
          condition STRING,
          total_dirty_records BIGINT,
          total_dirty_records_per DOUBLE,
          total_records BIGINT,
          dt STRING
        )
        USING DELTA
        PARTITIONED BY (type, mode, zone_stage, namespace_product, dataset, dt)
    """

    spark.sql(createTableSQL)
  }

  /**
   * Writes the metrics results into a delta table
   * @param validations A map containing all validations to be made
   * @param processInfo A map that holds process related information
   * @param metricsValues A metrics map holding all metrics results for all validated columns
   * @param totalRecords The total number of records validated
   * @param batchInfo Holds the batch's id and timestamp from streaming query progresses
   * @param spark A Spark session object implicitly given
   */
  def saveMetrics(validations: Validations,
                  processInfo: ProcessInfo,
                  metricsValues: MetricsValues,
                  totalRecords: Long,
                  batchInfo: (Long, String) = (-1, ""))(implicit spark: SparkSession): Unit = {
    val metricsDataframe: DataFrame = createMetricsDataFrame(validations, processInfo, metricsValues, totalRecords, batchInfo)

    createMetricsTableIfNotExists()

    metricsDataframe.write
      .format("delta")
      .mode("append")
      .partitionBy("type", "mode", "zone_stage", "namespace_product", "dataset", "dt")
      .saveAsTable(METRICS_TABLE)
  }
}
