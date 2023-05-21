package br.com.ttj
package dqv.validations

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

final case class Observation(constraint: String, name: String) {
  type Observer = org.apache.spark.sql.Observation

  private val tag: String = s"observation.$name"
  private val metric: String = tag

  private def observation: Column =
    expr(constraint)

  def compute: Column =
    sum(when(observation, 1).otherwise(0)).alias(metric)

  def observe(dataframe: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    if (dataframe.columns contains "_observations")
      dataframe.withColumn("_observations", map_concat($"_observations", map(lit(name), expr(constraint))))
    else
      dataframe.withColumn("_observations", map(lit(name), expr(constraint)))
  }

  def report(implicit observer: Observer): String = {
    val metricValue: Long = observer.get(tag).asInstanceOf[Long]

    s"👁️‍🗨️ Observation($name, $constraint): total of $metricValue records failed to pass the given constraint!"
  }
}
