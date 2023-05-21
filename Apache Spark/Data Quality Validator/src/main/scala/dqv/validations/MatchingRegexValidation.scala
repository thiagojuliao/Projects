package br.com.ttj
package dqv.validations

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

final case class MatchingRegexValidation(
                                          attr: String,
                                          pattern: String,
                                          override val threshold: Double
                                        ) extends Validation with ValidationMetric with ValidationReport {

  override val name: String = "is_matching_regex"
  override val tag: String = s"$name.$attr"
  override val metric: String = s"validation.$tag.dirty_records_count"

  override def validation: Column =
    when(col(attr).isNotNull, col(attr).rlike(pattern)).otherwise(true)

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

    s"$emoji isMatchingRegex($attr, $pattern): total of $metricValue dirty records found!"
  }
}
