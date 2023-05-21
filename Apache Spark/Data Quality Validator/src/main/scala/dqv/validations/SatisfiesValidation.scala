package br.com.ttj
package dqv.validations

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

import scala.util.matching.Regex

final case class SatisfiesValidation(
                                      constraint: String,
                                      alias: String,
                                      override val threshold: Double
                                    ) extends Validation with ValidationMetric with ValidationReport {

  override val name: String = "satisfies"
  override val tag: String = s"$name.$alias"
  override val metric: String = s"validation.$tag.dirty_records_count"

  override def validation: Column = {
    if (constraint contains "->") {
      val regex: Regex = "(.*)([!>=<]{1,2})(.*)->(.*)".r

      expr(
        constraint match {
          case regex(col1, op, value, col2) =>
            s"case when ${col1.trim} is null then true else (${col1.trim} ${op.trim} ${value.trim} and ${col2.trim}) end"
          case _ => "true"
        }
      )
    }
    else expr(constraint)
  }

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

    s"$emoji satisfies($alias, $constraint): total of $metricValue dirty records found!"
  }
}
