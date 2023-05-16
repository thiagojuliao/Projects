import core.SparkDataLineageListener
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.storage.StorageLevel

object Test00 extends App {

  /**
   * Initialization Code
   */

  implicit class LazyDisplay(dataframe: Dataset[_]) {
    def s: Unit = dataframe.show(5, truncate = false)
  }

  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("Test00")
    .getOrCreate()

  import spark.implicits._

  val sdll = new SparkDataLineageListener
  spark.listenerManager.register(sdll)

  /**
   * Production Code
   */

  val destinationPath = "src/main/resources/output/"

  val input00DF = spark.read
    .format("csv")
    .option("header", value = true)
    .load("src/main/resources/data/input00.csv")
    .filter(!$"application_id".like("%homolog"))
    .persist(StorageLevel.DISK_ONLY)

  input00DF.count()

  input00DF.repartition($"session_id").s

  val input01DF = spark.read
    .format("csv")
    .option("header", value = true)
    .load("src/main/resources/data/input01.csv")
    .filter($"merchant_id".isNotNull)
    .filter($"order_id".isNotNull)

  input01DF
    .groupBy("session_id")
    .agg(
      count("order_id").alias("total_orders")
    ).s

  val joinedDF = input00DF.join(input01DF, Seq("session_id", "dt"), "inner")

  joinedDF.write.format("parquet").mode("overwrite").partitionBy("dt").save(destinationPath)

  spark.read.parquet(destinationPath)
    .filter($"dt" === "2023-02-02")
    .groupBy("session_id")
    .agg(
      collect_set("order_id").alias("total_orders")
    )
    .filter(size($"total_orders") > 1)
    .withColumn("order_id", explode_outer($"total_orders"))
    .s


}
