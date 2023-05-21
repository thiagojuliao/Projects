package br.com.ttj
package dqv.validations

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

import scala.util.Try
import java.text.SimpleDateFormat

final case class FormattedAsDateValidation(
                                            attr: String,
                                            pattern: String,
                                            override val threshold: Double
                                          ) extends Validation with ValidationMetric with ValidationReport {

  override val name: String = "is_formatted_as_date"
  override val tag: String = s"$name.$attr"
  override val metric: String = s"validation.$tag.dirty_records_count"

  private def patternValidation: Option[Boolean] = Try {
    val sdf = new SimpleDateFormat(pattern)
    val date = new java.util.Date()

    sdf.format(date)
    true
  }.toOption

  override def validation: Column =
    coalesce(col(attr) === date_format(to_date(col(attr)), pattern), lit(false))

  override def compute: Column =
    sum(when(validation, 0).otherwise(1)).alias(metric)

  override def check(implicit observer: Observer): Boolean = {
    val totalRecords: Long = observer.get("global.total_records_count").asInstanceOf[Long]
    val metricValue: Long = observer.get(metric).asInstanceOf[Long]
    val ratio: Double = metricValue * 1.0 / totalRecords

    (0.0 <= ratio) & (ratio <= (1 - threshold))
  }

  override def report(implicit observer: Observer): String = {
    val emoji: String = getEmoji
    val metricValue: Long = observer.get(metric).asInstanceOf[Long]

    s"$emoji isFormattedAsDate($attr, $pattern): total of $metricValue dirty records found!"
  }

  assert(patternValidation.getOrElse(false), "Invalid pattern! Please input a valid date pattern.")
}
