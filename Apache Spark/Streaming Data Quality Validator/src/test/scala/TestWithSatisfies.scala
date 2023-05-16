import br.com.ifood.sdqv.core.StreamingDataQualityValidator
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object TestWithSatisfies extends App {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("Testing Satisfies")
    .master("local[*]")
    .getOrCreate()

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
    ),
    "satisfies" -> List(
      "order_total < 300", "order_total between 150 and 300", "items_total > 150 -> order_total > 150"
    )
  )

  val data: DataFrame = spark.read
    .format("csv")
    .option("header", "true")
    .load("src/main/resources/data/")
    .withColumn("dirty_columns", array(lit("test->a")))
    .withColumn("order_total", (rand() * 500).cast(IntegerType))
    .withColumn("items_total", (rand() * 350).cast(IntegerType))
    .withColumn("benefits_total", (rand() * 200).cast(IntegerType))

  val sdqv: StreamingDataQualityValidator = StreamingDataQualityValidator(data, validations, processInfo)

  val validatedDF: DataFrame = sdqv.validate()._1

  validatedDF.write.format("noop").mode("overwrite").save()

  sdqv.getMetricsDF.show(20, truncate = false)
  sdqv.getReportDF.show(20, truncate = false)
}
