package br.com.ifood.sdqv.core

import org.apache.spark.util.LongAccumulator

object GeneralTypes {

  type Validations = Map[String, Any]
  type ProcessInfo = Map[String, Any]
  type Metrics = Map[String, Map[String, LongAccumulator]]
  type MetricsValues = Map[String, Map[String, Long]]
}
