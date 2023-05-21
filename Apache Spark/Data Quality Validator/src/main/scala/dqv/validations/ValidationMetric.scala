package br.com.ttj
package dqv.validations

import org.apache.spark.sql.Column

trait ValidationMetric {
  val metric: String

  def compute: Column
}
