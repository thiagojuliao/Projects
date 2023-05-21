package br.com.ttj
package utils

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object Generators {

  def generateOrdersData(size: Long)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val schema: StructType = StructType(
      Seq(
        StructField("order_id", StringType),
        StructField("created_at", StringType),
        StructField("order_total", DoubleType),
        StructField("merchant_id", StringType),
        StructField("account_id", StringType)
      )
    )

    val rdd: RDD[Row] = spark.sparkContext.range(0, size).map { _ =>
      Row(
        generateNullableUUID, generateNullableTimestamp, generateNullableDouble.orNull,
        generateNullableUUID, generateNullableUUID
      )
    }

    spark.createDataFrame(rdd, schema)
      .withColumn("created_at", $"created_at".cast("timestamp"))
      .withColumn("order_total", round($"order_total", 2))
  }
}
