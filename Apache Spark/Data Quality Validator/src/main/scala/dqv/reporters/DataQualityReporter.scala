package br.com.ttj
package dqv.reporters

import dqv.validator.DataQualityValidator

trait DataQualityReporter {

  def report(validator: DataQualityValidator): Unit
}
