package br.com.ttj
package dqv.validations

trait ValidationReport {
  val threshold: Double

  type Observer = org.apache.spark.sql.Observation

  def getEmoji(implicit observer: Observer): String = {
    if (check) "✔️"
    else "❌"
  }

  def check(implicit observer: Observer): Boolean

  def report(implicit observer: Observer): String

  require((threshold >= 0.0) & (threshold <= 1.0))
}
