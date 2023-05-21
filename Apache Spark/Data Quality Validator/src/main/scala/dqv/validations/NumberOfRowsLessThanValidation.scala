package br.com.ttj
package dqv.validations

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

final case class NumberOfRowsLessThanValidation(
                                                 limit: Long
                                               ) extends Validation with ValidationMetric with ValidationReport {

  override val name: String = "has_number_of_rows_less_thah"
  override val tag: String = "global.$name"
  override val metric: String = "global.total_records_count"
  override val threshold: Double = 0.0

  override def validation: Column =
    lit(true)

  override def compute: Column =
    count(lit(1)).alias(metric)

  override def check(implicit observer: Observer): Boolean = {
    val totalRecords: Long = observer.get("global.total_records_count").asInstanceOf[Long]

    totalRecords < limit
  }

  override def report(implicit observer: Observer): String = {
    val emoji: String = getEmoji
    val totalRecords: Long = observer.get("global.total_records_count").asInstanceOf[Long]

    if (totalRecords >= limit)
      s"$emoji hasNumberOfRowsLessThan($limit): total of ${totalRecords - limit} records above the specified limit!"
    else
      s"$emoji hasNumberOfRowsLessThan($limit): total of ${limit - totalRecords} records below the specified limit!"
  }
}
