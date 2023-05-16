import org.apache.spark.sql.{DataFrame, SparkSession}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.io.Source

package object utils {

  object BuilderUtils {
    private val spark: Option[SparkSession] = SparkSession.getActiveSession

    require(spark.nonEmpty)

    def buildFromFile(filePath: String): (DataFrame, DataFrame) = {
      val file = new File(filePath)
      val scanner = Source.fromFile(file)

      val verticesRows = ListBuffer[(Int, Int)]()
      val edgeRows = ListBuffer[(Int, Int, Double)]()

      var rowCount = 1
      var columnCount = Int.MaxValue

      scanner.getLines().foreach { line =>
        val tokens = line.split(" ").zipWithIndex.filter(_._1 != "*").map { case (value, j) =>
          (rowCount, j + 1, value.toDouble)
        }

        verticesRows += ((rowCount, 1))
        edgeRows ++= tokens.toList

        rowCount += 1
        columnCount = Math.min(columnCount, line.split(" ").length)
      }

      scanner.close()

      assert(rowCount - 1 == columnCount, s"The number of rows and columns must be the same. Got $rowCount rows and $columnCount columns instead.")

      (
        spark.get.createDataFrame(verticesRows.toList).toDF("id", "dummy").drop("dummy"),
        spark.get.createDataFrame(edgeRows.toList).toDF("src", "dst", "cost")
      )
    }
  }
}
