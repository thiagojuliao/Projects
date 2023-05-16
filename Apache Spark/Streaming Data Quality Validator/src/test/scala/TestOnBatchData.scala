import br.com.ifood.sdqv.core.StreamingDataQualityValidator
import br.com.ifood.sdqv.server.LiveMetricsServer
import org.apache.spark.sql.functions.when
import org.apache.spark.sql.{DataFrame, SparkSession}

object TestOnBatchData extends App {

  val spark: SparkSession = SparkSession.builder()
    .appName("SDQV_TestOnBatch")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  /**
   * Sample Data
   */
  val data: DataFrame = spark.read
    .format("csv")
    .option("header", "true")
    .load("src/main/resources/data/")
    .withColumn("properties_is_pwa", when($"properties_is_pwa" === "null", null).otherwise($"properties_is_pwa"))

  data.show()

  /**
   * Parametrization
   */
  val processInfo: Map[String, Any] = Map(
    "name" -> "prd-etl-curated-consumer-sessions",
    "type" -> "batch",
    "mode" -> "legacy",
    "datasetInfo" -> Map(
      "zoneOrStage" -> "curated",
      "namespaceOrProduct" -> "consumer",
      "dataset" -> "sessions"
    )
  )

  val validations: Map[String, Any] = Map(
    "isNeverNull" -> List("id", "application_id", "device_id", "user_id", "dt"),
    "isAlwaysNull" -> List("properties_is_pwa"),
    "isMatchingRegex" -> Map(
      "id" -> "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$",
      "device_id" -> "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$",
      "user_id" -> "^[A-z|0-9]{8}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{4}-[A-z|0-9]{12}$"
    ),
    "isAnyOf" -> Map(
      "application_id" -> List("ifood-android", "ifood-ios")
    ),
    "isFormattedAsDate" -> Map(
      "dt" -> "yyyy-MM-dd"
    )
  )

  val sdqv: StreamingDataQualityValidator = StreamingDataQualityValidator(data, validations, processInfo)

  LiveMetricsServer.initialize(sdqv)

  val validatedDF: DataFrame = sdqv.validate()._1

  println(s"Total records: ${validatedDF.count()}")
  println(s"Total Records validated: ${sdqv.getTotalRecords}")

  sdqv.getReportDF.show()
  sdqv.getMetricsDF.show(20, truncate = false)

  LiveMetricsServer.saveMetrics(sdqv.getMetricsDF)

  Thread.sleep(15000)

  spark.read.table("default.data_quality_metrics").show(20, truncate = false)
}
