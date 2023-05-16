import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.expr

import java.io.{BufferedWriter, File, FileWriter}

object Playground extends App {

  def buildMotifPattern(size: Int = 1): String = {
    require(size >= 1)

    (1 to size)
      .map(i => s"(v$i)-[e$i]->(v${i + 1})")
      .reduce(_ + "; " + _)
  }

  println(buildMotifPattern())
  println(buildMotifPattern(2))
  println(buildMotifPattern(3))
  println(buildMotifPattern(4))
  println(buildMotifPattern(5))

  def buildMinCPExpression(size: Int = 1): Column = {
    require(size >= 1)

    val string = (1 to size)
      .map(i => s"e$i.cost")
      .mkString("(", " + ", ")") + s" / $size"

    expr(string)
  }

  println(buildMinCPExpression())
  println(buildMinCPExpression(2))
  println(buildMinCPExpression(3))
  println(buildMinCPExpression(4))
  println(buildMinCPExpression(5))

  val file = new File("src/main/resources/outputs/test.txt")
  val bw = new BufferedWriter(new FileWriter(file))

  bw.write(buildMotifPattern(2))
  bw.write("\n")
  bw.write(buildMotifPattern(3))

  bw.close()

  val vColumns = List("v1", "v2", "v3", "v4", "v5")

  println(
    (2 to 5).map { i =>
      val columnsToCheck = vColumns.filter(v => v != "v1" & v != s"v$i").map(_ + ".id")

      s"not array_contains(array(${columnsToCheck.mkString(", ")}), v$i.id)"
    }
      .reduce(_ + " and " + _)
  )
}
