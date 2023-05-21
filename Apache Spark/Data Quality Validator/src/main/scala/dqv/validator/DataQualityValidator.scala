package br.com.ttj
package dqv.validator

import dqv.metrics_store.DataQualityMetricsStore
import dqv.reporters.DataQualityReporter
import dqv.validations._
import dqv.validator.ValidationStrategies._

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Column, DataFrame, Row, SparkSession}

import java.text.SimpleDateFormat
import scala.collection.JavaConverters._
import scala.collection.mutable

object DataQualityValidator {

  class Builder {

    private val config: mutable.Map[String, Any] = mutable.Map()

    def name(name: String): Builder = {
      config += "name" -> name
      this
    }

    def displayName(displayName: String): Builder = {
      config += "displayName" -> displayName
      this
    }

    def info(name: String, value: String): Builder = {
      config += s"info.$name" -> value
      this
    }

    def validations(validations: DataQualityValidation*): Builder = {
      config += "validations" -> validations
      this
    }

    def observations(observations: Observation*): Builder = {
      config += "observations" -> observations
      this
    }

    def reporter(reporter: DataQualityReporter): Builder = {
      config += "reporter" -> reporter
      this
    }

    def metricsStore(ms: DataQualityMetricsStore): Builder = {
      config += "metricsStore" -> ms
      this
    }

    def strategy(validationStrategy: ValidationStrategy): Builder = {
      config += "strategy" -> validationStrategy
      this
    }

    private def getName: String =
      config.getOrElse("name", "data_quality_validator").asInstanceOf[String]

    private def getDisplayName: String =
      config.getOrElse("displayName", "Data Quality Validator").asInstanceOf[String]

    private def getAdditionalInfo: Map[String, String] = {
      val info: mutable.Map[String, String] = mutable.Map()

      config.keys.filter(_ contains "info.").foreach { key =>
        info += (key.replace("info.", "") -> config(key).toString)
      }

      info.toMap
    }

    private def getValidations: Seq[DataQualityValidation] = {
      val validations: Seq[DataQualityValidation] =
        config.getOrElse("validations", Seq[DataQualityValidation]()).asInstanceOf[Seq[DataQualityValidation]]

      assert(validations.nonEmpty, "No validations found! Please add some validations before trying to use this validator.")

      validations
    }

    private def getObservations: Seq[Observation] =
      config.getOrElse("observations", Seq[Observation]()).asInstanceOf[Seq[Observation]]

    private def getValidationStrategy: ValidationStrategy =
      config.getOrElse("strategy", TRACK_ONLY).asInstanceOf[ValidationStrategy]

    private def getReporter: Option[DataQualityReporter] =
      config.get("reporter").map(_.asInstanceOf[DataQualityReporter])

    private def getMetricsStore: Option[DataQualityMetricsStore] =
      config.get("metricsStore").map(_.asInstanceOf[DataQualityMetricsStore])

    def create(): DataQualityValidator = new DataQualityValidator(
      name = getName,
      displayName = getDisplayName,
      info = getAdditionalInfo,
      validations = getValidations,
      observations = getObservations,
      validationStrategy = getValidationStrategy,
      reporter = getReporter,
      metricsStore = getMetricsStore
    )
  }

  def builder(): Builder = new Builder
}

final class DataQualityValidator private (
                                           name: String,
                                           displayName: String,
                                           info: Map[String, String],
                                           validations: Seq[DataQualityValidation],
                                           observations: Seq[Observation],
                                           validationStrategy: ValidationStrategy,
                                           reporter: Option[DataQualityReporter],
                                           metricsStore: Option[DataQualityMetricsStore]
                                         ) {

  private val observer: org.apache.spark.sql.Observation =
    org.apache.spark.sql.Observation(name)

  private def getObservationMetrics: Map[String, Long] = {
    val metrics: mutable.Map[String, Long] = mutable.Map()

    observer.get.foreach { case (metric, value) =>
      if (metric contains "observation.")
        metrics += metric.replace("observation.", "") -> value.asInstanceOf[Long]
    }

    metrics.toMap
  }

  private def getValidationMetrics: Map[String, Map[String, Long]] = {
    val metrics: mutable.Map[String, Map[String, Long]] = mutable.Map()

    observer.get.foreach { case (metric, value) =>
      val value_ : Long = value.asInstanceOf[Long]

      if (metric contains "validation.") {
        val tokens: Array[String] = metric.split('.')
        val validation: String = tokens(1)
        val metricName: String = s"${tokens(2)}_${tokens(3)}"

        if (metrics contains validation)
          metrics(validation) += metricName -> value_
        else
          metrics += validation -> Map((metricName, value_))
      }
    }

    metrics.toMap
  }

  def getName: String = name

  def getDisplayName: String = displayName

  def getInfo: Map[String, String] = info

  def getValidations: Seq[DataQualityValidation] = validations

  def getObservations: Seq[Observation] = observations

  def getObserver: org.apache.spark.sql.Observation = observer

  def getReporter: DataQualityReporter = reporter.orNull

  def getMetricsStore: DataQualityMetricsStore = metricsStore.orNull

  def apply(dataframe: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val observed: DataFrame =
      observations.foldLeft(dataframe) { (result, observation) =>
        observation.observe(result)
      }

    val validated: DataFrame = {
      validations.foldLeft(observed) { (result, validation) =>
        validation.validate(result)
      }
    }

    val totalRecordsCount: Column =
      count(lit(1)).alias("global.total_records_count")

    val metricsToCompute: Seq[Column] =
      validations.map(_.compute) ++ observations.map(_.compute)

    validationStrategy match {
      case DROP_DIRTY =>
        validated.observe(observer, totalRecordsCount, metricsToCompute: _*).filter(!$"_dirty_record")
      case _ =>
        validated.observe(observer, totalRecordsCount, metricsToCompute: _*)
    }
  }

  def getValidationResult: Map[String, Long] = {
    val totalRecords: Long = observer.get("global.total_records_count").asInstanceOf[Long]

    val totalDistinctRecords: Long =
      observer.get.getOrElse("global.total_distinct_records_count", totalRecords).asInstanceOf[Long]

    val totalDirtyRecords: Long =
      (
        observer.get.filter(kv => kv._1 contains "validation.").values
          .map(_.asInstanceOf[Long]) ++ Seq(totalRecords - totalDistinctRecords)
        ).max

    val totalCleanRecords: Long = totalRecords - totalDirtyRecords

    val totalDroppedRecords: Long = validationStrategy match {
      case DROP_DIRTY => totalDirtyRecords
      case _ => 0L
    }

    Map(
      "total_records" -> totalRecords,
      "total_distinct_records" -> totalDistinctRecords,
      "total_clean_records" -> totalCleanRecords,
      "total_dirty_records" -> totalDirtyRecords,
      "total_dropped_records" -> totalDroppedRecords
    )
  }

  def runWith(dataframe: DataFrame, dropAuxColumns: Boolean = true)(writer: DataFrame => Unit)(implicit spark: SparkSession): Unit = {

    val validatedDF: DataFrame = {
      if (dropAuxColumns) apply(dataframe).drop("_validations", "_observations")
      else apply(dataframe)
    }

    writer(validatedDF)

    if (reporter.nonEmpty) reporter.get.report(this)
    if (metricsStore.nonEmpty) metricsStore.get.save(this)

    validationStrategy match {
      case FAIL =>
        val maybeFail: Boolean = !validations.forall(_.check(observer))
        if (maybeFail) throw new RuntimeException("Dirty records found and the chosen strategy was FAIL. Aborting execution...")
      case _ =>
    }
  }

  def getMetricsDF(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val schema: StructType = StructType(
      Seq(
        StructField("id", StringType),
        StructField("additional_info", MapType(StringType, StringType)),
        StructField("validation_metrics", MapType(StringType, MapType(StringType, LongType))),
        StructField("observation_metrics", MapType(StringType, LongType)),
        StructField("validation_result", MapType(StringType, LongType)),
        StructField("report_date", StringType)
      )
    )

    val id: String = getName
    val info: Map[String, String] = getInfo
    val observationMetrics: Map[String, Long] = getObservationMetrics
    val validationMetrics: Map[String, Map[String, Long]] = getValidationMetrics
    val validationResult: Map[String, Long] = getValidationResult

    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val reportDate: String = sdf.format(new java.util.Date())

    val data: java.util.List[Row] = List(
      Row(
        id, info, validationMetrics, observationMetrics, validationResult, reportDate
      )
    ).asJava

    spark.createDataFrame(data, schema)
      .withColumn("report_date", to_date($"report_date"))
  }
}
