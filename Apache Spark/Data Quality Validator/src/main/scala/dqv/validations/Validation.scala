package br.com.ttj
package dqv.validations

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

trait Validation { self: ValidationMetric with ValidationReport =>
  val name: String
  val tag: String

  def validation: Column

  def validate(dataframe: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    if (dataframe.columns contains "_validations")
      dataframe
        .withColumn("_validations", map_concat($"_validations", map(lit(tag), validation)))
        .withColumn("_dirty_record", $"_dirty_record" || !validation)
    else
      dataframe
        .withColumn("_validations", map(lit(tag), validation))
        .withColumn("_dirty_record", !validation)
  }
}
