package br.com.ttj
package dqv.validations

import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.{Window, WindowSpec}

final case class UniqueKeyValidation(
                                      keys: Seq[String],
                                      override val threshold: Double
                                    ) extends Validation with ValidationMetric with ValidationReport {

  override val name: String = "has_unique_key"
  override val tag: String = s"global.$name"
  override val metric: String = "global.total_distinct_records_count"

  private def nullSafeKeysEval: Seq[Column] =
    keys.map(attr => coalesce(col(attr).cast("string"), lit("")))

  override def validation: Column =
    lit(true)

  override def compute: Column =
    size(collect_set(md5(concat(nullSafeKeysEval:_*)))).cast("long").alias(metric)

  override def validate(dataframe: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val w: WindowSpec = Window.partitionBy(keys.map(col):_*).orderBy(lit(1))

    if (dataframe.columns contains "_validations") {
      val _validations: Column =
        when($"rn" === 1, map_concat($"_validations", map(lit(name), lit(true))))
          .otherwise(map_concat($"_validations", map(lit(name), lit(false))))

      val _dirty_record: Column =
        when($"rn" === 1, $"_dirty_record" || lit(false)).otherwise(lit(true))

      dataframe
        .withColumn("rn", row_number().over(w))
        .withColumn("_validations", _validations)
        .withColumn("_dirty_record", _dirty_record)
        .drop("rn")
    }
    else {
      val _validations: Column =
        when($"rn" === 1, map(lit(name), lit(true))).otherwise(map(lit(name), lit(false)))

      val _dirty_record: Column =
        when($"rn" === 1, lit(false)).otherwise(lit(true))

      dataframe
        .withColumn("rn", row_number().over(w))
        .withColumn("_validations", _validations)
        .withColumn("_dirty_record", _dirty_record)
        .drop("rn")
    }
  }

  override def check(implicit observer: Observer): Boolean = {
    val totalRecords: Long = observer.get("global.total_records_count").asInstanceOf[Long]
    val metricValue: Long = observer.get(metric).asInstanceOf[Long]
    val ratio: Double = (totalRecords - metricValue) * 1.0 / totalRecords

    (0.0 <= ratio) & (ratio <= (1 - threshold))
  }

  override def report(implicit observer: Observer): String = {
    val emoji: String = getEmoji
    val totalRecords: Long = observer.get("global.total_records_count").asInstanceOf[Long]
    val metricValue: Long = observer.get(metric).asInstanceOf[Long]

    s"$emoji hasUniqueKey(${keys.mkString(", ")}): total of ${totalRecords - metricValue} non-unique records found!"
  }
}
