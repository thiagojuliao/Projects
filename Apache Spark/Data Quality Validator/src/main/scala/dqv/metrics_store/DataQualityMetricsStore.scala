package br.com.ttj
package dqv.metrics_store

import dqv.validator.DataQualityValidator

trait DataQualityMetricsStore {

  def save(validator: DataQualityValidator): Unit
}
