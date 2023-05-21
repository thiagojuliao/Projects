package br.com.ttj

import dqv.metrics_store.FileMetricsStore
import dqv.reporters._
import dqv.validations._
import dqv.validator.DataQualityValidator
import dqv.validator.ValidationStrategies._
import utils._

import org.apache.spark.sql.{DataFrame, SparkSession}

object ValidatorTest  extends App {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("Data Quality Validator Test")
    .master("local[*]")
    .getOrCreate()

  val uuidRegex: String = "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$"

  val fileMS: FileMetricsStore = new FileMetricsStore(
    filePath = "src/main/resources/metrics/",
    format = "csv",
    separator = ","
  )

  val dqv: DataQualityValidator = DataQualityValidator.builder()
    .name("dqv_test")
    .displayName("DQV Test")
    .info("testing_date", "2023-05-19")
    .validations(
      hasUniqueKey(Seq("order_id", "created_at")),
      isNeverNull("order_id"),
      isNeverNull("created_at"),
      isMatchingRegex("order_id", uuidRegex),
      isMatchingRegex("merchant_id", uuidRegex),
      isMatchingRegex("account_id", uuidRegex),
      satisfies("order_total > 0", "valid_order_total"),
      hasNumOfRowsGreatherThan(5000)
    )
    .reporter(ConsoleReporter)
    .metricsStore(fileMS)
    .strategy(DROP_DIRTY)
    .create()

  val ordersData: DataFrame = Generators.generateOrdersData(1000000)
  val testWriter: DataFrame => Unit = _.write.format("noop").mode("overwrite").save()

  dqv.runWith(ordersData)(testWriter)

  dqv.getMetricsDF.show(1, truncate = false)
}
