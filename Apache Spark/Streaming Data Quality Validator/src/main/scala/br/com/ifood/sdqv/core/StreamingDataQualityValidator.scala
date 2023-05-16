package br.com.ifood.sdqv.core

import br.com.ifood.sdqv.core.GeneralTypes._
import br.com.ifood.sdqv.utils.StreamingDataQualityValidatorUtils
import org.apache.spark.sql._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.util.LongAccumulator

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

case class StreamingDataQualityValidator(dataframe: DataFrame,
                                         validations: Validations,
                                         processInfo: ProcessInfo) extends Logging {
  import GenericValidationFunctions._
  import StreamingDataQualityValidatorUtils._
  import UserDefinedFunctionWrappers._

  require(validations.nonEmpty)
  require(processInfo.nonEmpty)

  private implicit val spark: SparkSession = SparkSession.builder().getOrCreate()
  import spark.implicits._

  private val reportSchema : StructType = StructType(
    Seq(
      StructField("columns or conditions", StringType),
      StructField("isNeverNull (dirty records found)", LongType),
      StructField("isNeverNull %", StringType),
      StructField("isAlwaysNull (dirty records found)", LongType),
      StructField("isAlwaysNull %", StringType),
      StructField("isAnyOf (dirty records found)", LongType),
      StructField("isAnyOf %", StringType),
      StructField("isMatchingRegex (dirty records found)", LongType),
      StructField("isMatchingRegex %", StringType),
      StructField("isFormattedAsDate (dirty records found)", LongType),
      StructField("isFormattedAsDate %", StringType),
      StructField("satisfies (dirty records found)", LongType),
      StructField("satisfies %", StringType)
    )
  )

  private val columns: List[String] = dataframe.columns.toList
  private val metricsMap: Metrics = createMetricsMap(validations)
  private val totalRecords: LongAccumulator = createLongAccumulator("Total Records")

  /**
   * Increments the total records count by 1
   */
  private def addRecord(): Unit =
    totalRecords.add(1)

  /**
   * Applies a given validation built as an User Defined Function on the input dataframe
   * @param dataframe The input dataframe to be validated
   * @param columns List of columns to be checked against the validation rule
   * @param f User Defined Function representing the validation to be made
   * @return The dataframe with the UDF validation applied
   */
  private def applyUserDefinedFunctionValidation(dataframe: DataFrame, columns: List[Column], f: Validations => List[Any] => List[String]): DataFrame = {
    columns.size match {
      case 0 =>
        dataframe
      case 1 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations1(f(validations))(columns:_*)))
      case 2 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations2(f(validations))(columns:_*)))
      case 3 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations3(f(validations))(columns:_*)))
      case 4 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations4(f(validations))(columns:_*)))
      case 5 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations5(f(validations))(columns:_*)))
      case 6 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations6(f(validations))(columns:_*)))
      case 7 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations7(f(validations))(columns:_*)))
      case 8 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations8(f(validations))(columns:_*)))
      case 9 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations9(f(validations))(columns:_*)))
      case 10 =>
        dataframe.withColumn("dirty_columns", array_union($"dirty_columns", applyValidations10(f(validations))(columns:_*)))
      case _ =>
        throw new RuntimeException(s"The number of columns to validate (${columns.size}) exceeds the maximum allowed in the current implementation: 10")
    }
  }

  /**
   * Auxiliary function to retrieve all columns to be validated from a given rule
   * @param validation Validation's name
   * @return List of spark columns to be checked against the validation rule
   */
  private def getValidationColumns(validation: String): List[Column] = {
    if (validations.contains(validation))
      validations(validation).asInstanceOf[Map[String, Any]].keys.map(col).toList
    else
      List()
  }

  /**
   * Gets the result by applying all validations on a given dataframe
   * @param dataframe The input dataframe to be validated
   * @return The dataframe checked against all validation rules
   */
  private def getResult(dataframe: DataFrame): DataFrame = {
    applyUserDefinedFunctionValidation(
      applyUserDefinedFunctionValidation(
        satisfies(
          isAnyOf(
            isAlwaysNull(
              isNeverNull(
                dataframe.withColumn("dirty_columns", array().cast(ArrayType(StringType))), validations
              ), validations
            ), validations
          ), validations
        ), getValidationColumns("isFormattedAsDate"), isFormattedAsDate
      ), getValidationColumns("isMatchingRegex"), isMatchingRegex
    )
  }

  /**
   * User Defined Function to count clean and dirty records from the validated dataframe
   * @return Returns the current status flag for a record after validation
   */
  private def countCleanAndDirtyRecords: UserDefinedFunction = udf((dirtyColumns: List[String], tag: Boolean) => {
    addRecord()

    if (dirtyColumns.nonEmpty) {
      dirtyColumns.foreach { dirtyMark =>
        val validationAndColumn: Array[String] = dirtyMark.split("->")
        val validation: String = validationAndColumn(0)
        val column: String = validationAndColumn(1)

        metricsMap(validation)(column).add(1)
      }
    }

    tag
  })

  /**
   * Gets the metrics results map
   * @return A map holding all metrics and their results
   */
  def getMetricsValues: MetricsValues = {
    // Scala 2.13 > Only!
    //    metricsMap.view.mapValues { columnsAndAccumulators =>
    //      columnsAndAccumulators.mapValues(_.value)
    //    }.asInstanceOf[MetricsValues]

    metricsMap.view.map { metrics =>
      (
        metrics._1,
        metrics._2.map { columnAndAccumulator =>
          (columnAndAccumulator._1, columnAndAccumulator._2.value)
        }
      )
    }.toMap.asInstanceOf[MetricsValues]
  }

  /**
   * Gets the total records count
   * @return The total record count
   */
  def getTotalRecords: Long =
    totalRecords.value

  /**
   * Resets all accumulators from the metrics map
   */
  def resetAccumulators(): Unit = {
    totalRecords.reset()

    metricsMap.foreach { validationsAndColumns =>
      validationsAndColumns._2.foreach { columnAndAccumulator =>
        val accumulator: LongAccumulator = columnAndAccumulator._2

        accumulator.reset()
      }
    }
  }

  /**
   * Helper function to get the current dirty records count from a specific metric validated on a specific column
   * @param column Column name from which the metric refers to
   * @param metric The validation metric applied on the given column
   * @return The total dirty records count
   */
  private def getColumnMetricTotal(column: String, metric: String): Long = {
    if (metricsMap(metric).contains(column)) metricsMap(metric)(column).value
    else 0L
  }

  /**
   * Builds and returns the metrics dataframe from the metrics map results
   * @return A dataframe containing all metrics results
   */
  def getMetricsDF: DataFrame = {
    val totalRecords: Long = getTotalRecords
    val batchInfo: (Long, String) = getLastBatchInfo(processInfo)
    val metricsValues: MetricsValues = getMetricsValues

    createMetricsDataFrame(validations, processInfo, metricsValues, totalRecords, batchInfo)
  }

  /**
   * Builds and returns the report dataframe based on the results collected from all validations
   * @return A report dataframe
   */
  def getReportDF: DataFrame = {
    implicit val totalRecords: Long = getTotalRecords

    val rows: ListBuffer[Row] = ListBuffer()

    var isNeverNullTotal: Long = 0L
    lazy val isNeverNullPer: String = getTotalPer(isNeverNullTotal)

    var isAlwaysNullTotal: Long = 0L
    lazy val isAlwaysNullPer: String = getTotalPer(isAlwaysNullTotal)

    var isAnyOfTotal: Long = 0L
    lazy val isAnyOfPer: String = getTotalPer(isAnyOfTotal)

    var isMatchingRegexTotal: Long = 0L
    lazy val isMatchingRegexPer: String = getTotalPer(isMatchingRegexTotal)

    var isFormattedAsDateTotal: Long = 0L
    lazy val isFormattedAsDatePer: String = getTotalPer(isFormattedAsDateTotal)

    var satisfiesTotal: Long = 0L
    lazy val satisfiesPer: String = getTotalPer(satisfiesTotal)

    columns.foreach { column =>
      val isNeverNullColumnTotal: Long = getColumnMetricTotal(column, "isNeverNull")
      val isNeverNullColumnPer: String = getTotalPer(isNeverNullColumnTotal)

      val isAlwaysNullColumnTotal: Long = getColumnMetricTotal(column, "isAlwaysNull")
      val isAlwaysNullColumnPer: String = getTotalPer(isAlwaysNullColumnTotal)

      val isAnyOfColumnTotal: Long = getColumnMetricTotal(column, "isAnyOf")
      val isAnyOfColumnPer: String = getTotalPer(isAnyOfColumnTotal)

      val isMatchingRegexColumnTotal: Long = getColumnMetricTotal(column, "isMatchingRegex")
      val isMatchingRegexColumnPer: String = getTotalPer(isMatchingRegexColumnTotal)

      val isFormattedAsDateColumnTotal: Long = getColumnMetricTotal(column, "isFormattedAsDate")
      val isFormattedAsDateColumnPer: String = getTotalPer(isFormattedAsDateColumnTotal)

      isNeverNullTotal += isNeverNullColumnTotal
      isAlwaysNullTotal += isAlwaysNullColumnTotal
      isAnyOfTotal += isAnyOfColumnTotal
      isMatchingRegexTotal += isMatchingRegexColumnTotal
      isFormattedAsDateTotal += isFormattedAsDateColumnTotal

      rows += Row(
        column, isNeverNullColumnTotal, isNeverNullColumnPer, isAlwaysNullColumnTotal, isAlwaysNullColumnPer,
        isAnyOfColumnTotal, isAnyOfColumnPer, isMatchingRegexColumnTotal, isMatchingRegexColumnPer,
        isFormattedAsDateColumnTotal, isFormattedAsDateColumnPer, 0L, "0.0"
      )
    }

    if (metricsMap.contains("satisfies")) {
      metricsMap("satisfies").foreach { pair =>
        val condition: String = pair._1
        val satisfiesColumnTotal: Long = getColumnMetricTotal(condition, "satisfies")
        val satisfiesColumnPer: String = getTotalPer(satisfiesColumnTotal)

        satisfiesTotal += satisfiesColumnTotal

        rows += Row(
          condition, 0L, "0.0", 0L, "0.0", 0L, "0.0", 0L, "0.0", 0L, "0.0", satisfiesColumnTotal, satisfiesColumnPer
        )
      }
    }

    rows += Row(
      "Metric Total", isNeverNullTotal, isNeverNullPer, isAlwaysNullTotal, isAlwaysNullPer,
      isAnyOfTotal, isAnyOfPer, isMatchingRegexTotal, isMatchingRegexPer, isFormattedAsDateTotal, isFormattedAsDatePer,
      satisfiesTotal, satisfiesPer
    )

    spark.createDataFrame(rows.toList.asJava, reportSchema)
  }

  /**
   * Main method for validation.
   *
   * Given an input dataframe all rows will be checked against all metrics within the validations map.
   *
   * The user may choose to split the validated dataframe in two (one with only clean records and another with only dirty records)
   * or getting the same input dataframe but with all rows tagged with either True or False values specifying if the given row is clean or not.
   * @param autoCleaning Splits the validated dataframe in two: one with only clean records and another with only dirty records or
   *                     return the validated dataframe with all rows tagged
   * @return A tuple from one of the possible choices: (cleanDF, dirtyDF) or (taggedDF, EmptyDF)
   */
  def validate(autoCleaning: Boolean = false): (DataFrame, DataFrame) = {
    resetAccumulators()

    val validatedDF: DataFrame = getResult(dataframe)

    if (autoCleaning) {
      val cleanedDF: DataFrame = validatedDF
        .filter(size($"dirty_columns") === 0)
        .filter(countCleanAndDirtyRecords($"dirty_columns", lit(true)))
        .withColumn("is_clean_record", lit(true))
        .drop("dirty_columns")

      val dirtyDF: DataFrame = validatedDF
        .filter(size($"dirty_columns") > 0)
        .filter(countCleanAndDirtyRecords($"dirty_columns", lit(true)))
        .withColumn("is_clean_record", lit(false))
        .drop("dirty_columns")

      (cleanedDF, dirtyDF)
    } else {
      val cleanAndDirtyDF: DataFrame = validatedDF
        .filter(countCleanAndDirtyRecords($"dirty_columns", lit(true)))
        .withColumn("is_clean_record", when(size($"dirty_columns") === 0, true).otherwise(false))
        .drop("dirty_columns")

      (cleanAndDirtyDF, spark.emptyDataFrame)
    }
  }

  override def toString: String =
    "Streaming Data Quality Validator v.2.0.1"
}