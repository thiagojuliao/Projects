package br.com.ifood.sdqv.utils

import br.com.ifood.sdqv.core.GeneralTypes._

import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.util.LongAccumulator
import org.apache.spark.sql.{DataFrame, SparkSession, Row}
import org.apache.spark.sql.streaming.{StreamingQuery, StreamingQueryProgress}

import java.time.LocalDateTime

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap, Set => MutableSet, ListBuffer}

case object StreamingDataQualityValidatorUtils {

  /**
   * Schema definition for the Metrics Table
   */
  private val metricsTableSchema: StructType = StructType(
    Seq(
      StructField("process_name", StringType),
      StructField("type", StringType),
      StructField("mode", StringType),
      StructField("zone_stage", StringType),
      StructField("namespace_product", StringType),
      StructField("dataset", StringType),
      StructField("batch_id", LongType),
      StructField("timestamp", StringType),
      StructField("column", StringType),
      StructField("metric", StringType),
      StructField("condition", StringType),
      StructField("total_dirty_records", LongType),
      StructField("total_dirty_records_per", DoubleType),
      StructField("total_records", LongType)
    )
  )

  /**
   * Extracts the dataset name from a map containing process related information
   * @param processInfo A map that holds process related information
   * @return A formatted string like "namespace_zone.dataset"
   */
  def getDatasetName(processInfo: ProcessInfo): String = {
    val datasetInfo: Map[String, String] = processInfo("datasetInfo").asInstanceOf[Map[String, String]]
    val zoneOrStage: String = datasetInfo("zoneOrStage")
    val namespaceOrProduct: String = datasetInfo("namespaceOrProduct")
    val dataset: String = datasetInfo("dataset")

    s"${namespaceOrProduct}_$zoneOrStage.$dataset"
  }

  /**
   * Returns the percentage related to a numeric division
   * @param numerator The numerator's value
   * @param denominator The denominator's value implicitly given
   * @return A formatted string like "2.52"
   */
  def getTotalPer(numerator: Long)(implicit denominator: Long): String =
    if (denominator != 0) "%.2f".format(numerator * 1.0 / denominator * 100) else "0.00"

  /**
   * Gets the last batch detailed information from a underlying streaming application
   * @param processInfo A map that holds process related information
   * @param spark A Spark session object implicitly given
   * @return A tuple (batchId, timestamp)
   */
  def getLastBatchInfo(processInfo: ProcessInfo)(implicit spark: SparkSession): (Long, String) = {
    val queryName: String = getDatasetName(processInfo)
    val maybeStreamingQuery: Option[StreamingQuery] = spark.streams.active.find(_.name == queryName)

    if (maybeStreamingQuery.isDefined) {
      val streamProgress: Option[StreamingQueryProgress] = Option(maybeStreamingQuery.get.lastProgress)

      if (streamProgress.isDefined) {
        val batchId: Long = streamProgress.get.batchId
        val timestamp: String = streamProgress.get.timestamp

        (batchId, timestamp)
      } else (-1L, "")
    } else (-1L, "")
  }

  /**
   * Creates the column mapping with their respective indices from a column name list
   * @param columns List containing all the column names from the input dataframe
   * @return A map with (index -> column) key value pairs
   */
  def createColumnMapping(columns: List[String]): Map[Int, String] = {
    val columnMapping: MutableMap[Int, String] = MutableMap()

    for (index <- columns.indices) {
      columnMapping += (index -> columns(index))
    }

    columnMapping.toMap
  }

  /**
   * Return the columns to be validated given a validations map
   * @param validations A map containing all validations to be made
   * @return A Set of columns to be validated
   */
  def getColumnsToBeValidated(validations: Validations): Set[String] = {
    val columnsToBeValidated: MutableSet[String] = MutableSet()

    validations.foreach { kv =>
      val validation: String = kv._1
      val value: Any = kv._2

      value match {
        case _: List[_] if validation != "satisfies" =>
          columnsToBeValidated ++= value.asInstanceOf[List[String]].toSet
        case _: Map[_, _] =>
          columnsToBeValidated ++= value.asInstanceOf[Map[String, Any]].keys.toSet
        case _ =>
      }
    }

    columnsToBeValidated.toSet
  }

  /**
   * Creates a long accumulator with a given name
   * @param name The name to be given to this accumulator
   * @param spark A Spark session object implicitly given
   * @return A Long typed accumulator
   */
  def createLongAccumulator(name: String)(implicit spark: SparkSession): LongAccumulator =
    spark.sparkContext.longAccumulator(name)

  /**
   * Composes an accumulator's name given a specific rule, column and an optional condition value
   * @param rule The validation rule from which this accumulator refers to
   * @param column The column to be validated
   * @param value If the validation requires a conditional value to be met it'll be referred to
   * @return A formatted string like "isNeverNull(user_id)"
   */
  def getAccumulatorName(rule: String, column: String, value: Option[String] = None): String =
    if (value.isDefined) s"$rule($column, ${value.get}) dirty records"
    else s"$rule($column) dirty records"

  /**
   * Creates the metrics map that will hold all the current dirty record counts for every validation
   * @param validations A map containing all validations to be made
   * @param spark A Spark session object implicitly given
   * @return A metrics map holding all metrics results for all validated columns
   */
  def createMetricsMap(validations: Validations)(implicit spark: SparkSession): Metrics = {
    val metricsMap: MutableMap[String, Map[String, LongAccumulator]] = MutableMap()
    val rules = validations.keys.toList

    rules.foreach { rule =>
      val maybeColumnsList = validations(rule)

      if (!metricsMap.contains(rule))
        metricsMap += (rule -> Map())

      maybeColumnsList match {
        case _: List[_] =>
          maybeColumnsList.asInstanceOf[List[String]].foreach { column =>
            val accumulatorName = getAccumulatorName(rule, column)
            val longAccumulator = createLongAccumulator(accumulatorName)

            if (rule != "satisfies")
              metricsMap(rule) += (column -> longAccumulator)
            else
              metricsMap(rule) += (column.replace("->", "=>") -> longAccumulator)
          }
        case _: Map[_, _] =>
          val columnsMap = maybeColumnsList.asInstanceOf[Map[String, String]]
          val columnsList = columnsMap.keys.toList

          columnsList.foreach { column =>
            val accumulatorName = getAccumulatorName(rule, column, Some(columnsMap(column)))
            val longAccumulator = createLongAccumulator(accumulatorName)

            metricsMap(rule) += (column -> longAccumulator)
          }
        case _ =>
      }
    }

    metricsMap.toMap
  }

  /**
   * Creates the metrics dataframe given a metrics map
   * @param validations A map containing all validations to be made
   * @param processInfo A map that holds process related information
   * @param metricsValues A metrics map holding all metrics results for all validated columns
   * @param totalRecords The total number of records validated
   * @param batchInfo Holds the batch's id and timestamp from streaming query progresses
   * @param spark A Spark session object implicitly given
   * @return A dataframe containing the results of all validations made
   */
  def createMetricsDataFrame(validations: Validations,
                             processInfo: ProcessInfo,
                             metricsValues: MetricsValues,
                             totalRecords: Long,
                             batchInfo: (Long, String) = (-1, ""))(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val processName: String = processInfo("name").toString
    val processType: String = processInfo("type").toString
    val processMode: String = processInfo("mode").toString

    val datasetInfo: Map[String, String] = processInfo("datasetInfo").asInstanceOf[Map[String, String]]
    val zoneOrStage: String = datasetInfo("zoneOrStage")
    val namespaceOrProduct: String = datasetInfo("namespaceOrProduct")
    val dataset: String = datasetInfo("dataset")

    val batchId: Long = if (batchInfo._1 < 0) 0 else batchInfo._1
    val timestamp: String = if (batchInfo._2.isEmpty) LocalDateTime.now().toString + "+0000" else batchInfo._2.replace("Z", "+0000")

    val rows: ListBuffer[Row] = ListBuffer()

    val columnsToBeValidated: List[String] = getColumnsToBeValidated(validations).toList

    columnsToBeValidated.foreach { column =>
      val metrics: List[String] = validations.keys.toList

      metrics.foreach { metric =>
        if (metricsValues(metric).contains(column)) {
          val condition: String = metric match {
            case "isAnyOf" =>
              validations(metric).asInstanceOf[Map[String, List[String]]](column).toString
            case "isMatchingRegex" =>
              validations(metric).asInstanceOf[Map[String, String]](column)
            case "isFormattedAsDate" =>
              validations(metric).asInstanceOf[Map[String, String]](column)
            case _ => ""
          }

          val totalDirtyRecords: Long = metricsValues(metric)(column)
          val totalDirtyRecordsPer: Double = if (totalRecords > 0) totalDirtyRecords * 1.0 / totalRecords else 0

          rows += Row(
            processName, processType, processMode, zoneOrStage, namespaceOrProduct, dataset, batchId,
            timestamp, column, metric, condition, totalDirtyRecords, totalDirtyRecordsPer, totalRecords
          )
        }
      }
    }

    if (metricsValues.contains("satisfies")) {
      metricsValues("satisfies").foreach { pair =>
        val condition: String = pair._1
        val totalDirtyRecords: Long = pair._2
        val totalDirtyRecordsPer: Double = if (totalRecords > 0) totalDirtyRecords * 1.0 / totalRecords else 0

        rows += Row(
          processName, processType, processMode, zoneOrStage, namespaceOrProduct, dataset, batchId,
          timestamp, "", "satisfies", condition, totalDirtyRecords, totalDirtyRecordsPer, totalRecords
        )
      }
    }

    spark.createDataFrame(rows.toList.asJava, metricsTableSchema)
      .withColumn("total_dirty_records_per", round($"total_dirty_records_per" * 100, 2))
      .withColumn("dt", date_format($"timestamp", "yyyy-MM-dd"))
  }
}
