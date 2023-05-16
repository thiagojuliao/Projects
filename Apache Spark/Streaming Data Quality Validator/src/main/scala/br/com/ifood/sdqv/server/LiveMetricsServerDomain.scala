package br.com.ifood.sdqv.server

import org.apache.spark.sql.DataFrame

object LiveMetricsServerDomain {

  case object TimerKey
  case object CheckForStreamingProgress

  case class SaveMetrics(dataframe: DataFrame)
}
