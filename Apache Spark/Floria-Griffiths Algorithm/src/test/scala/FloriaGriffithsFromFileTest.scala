import core._
import org.apache.spark.sql.SparkSession

object FloriaGriffithsFromFileTest extends App {
  val spark = SparkSession.builder()
    .master("local[*]")
    .appName("FloriaGriffithsFromFileTest")
    .getOrCreate()

  spark.sparkContext.setLogLevel("INFO")

  // Matrix 00 run
  val matrix00 = "src/main/resources/inputs/matrix00.txt"

  val fg00 = FloriaGriffiths.fromFile(matrix00)

  fg00.run()

  val transformedCostMatrix00 = transformCostMatrix(fg00.graph.edges, fg00.u, fg00.mc)
  printAllMinimalCycles(transformedCostMatrix00, "src/main/resources/outputs/matrix00_cycles.txt")

  fg00.clear()

  // Matrix 01 Run
  val matrix01 = "src/main/resources/inputs/matrix01.txt"

  val fg01 = FloriaGriffiths.fromFile(matrix01)

  fg01.run()

  val transformedCostMatrix01 = transformCostMatrix(fg01.graph.edges, fg01.u, fg01.mc)
  printAllMinimalCycles(transformedCostMatrix01, "src/main/resources/outputs/matrix01_cycles.txt")
}
