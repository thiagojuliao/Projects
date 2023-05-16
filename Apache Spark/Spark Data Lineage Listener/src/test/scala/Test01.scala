import core.SparkDataLineageListener
import org.apache.spark.sql.expressions.{Window, WindowSpec}
import org.apache.spark.sql.functions.collect_set
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object Test01 extends App {
  implicit class LazyDisplay(dataframe: Dataset[_]) {
    def s: Unit = dataframe.show(5, truncate = false)
  }

  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("Test01")
    .getOrCreate()

  import spark.implicits._

  val sdll = new SparkDataLineageListener
  spark.listenerManager.register(sdll)

  val destinationPath = "src/main/resources/output/"

  val input00DF: Dataset[Row] = spark.read
    .format("csv")
    .option("header", value = true)
    .load("src/main/resources/data/input00.csv")
    .filter(!$"application_id".like("%homolog"))

  val window: WindowSpec = Window.partitionBy("device_id")

  input00DF
    .select("device_id", "session_id")
    .repartition($"device_id")
    .withColumn("session_ids", collect_set("session_id").over(window))
    .drop("session_id")
    .distinct()
    .s
}
