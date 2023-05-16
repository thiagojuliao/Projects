import implicits._
import org.apache.spark.sql.SparkSession
import org.graphframes.GraphFrame

object GraphFrameTest extends App {
  val spark = SparkSession.builder()
    .master("local[*]")
    .appName("Floria-Griffiths")
    .getOrCreate()

  val vertex = spark.createDataFrame(List(
      (1, 1), (2, 1), (3, 1), (4, 1), (5, 1)
    ))
    .toDF("id", "dummy").drop("dummy")

  val edges = spark.createDataFrame(
    List(
      (1, 2, 1.5), (1, 3, 4.0), (1, 4, 1.5), (1, 5, 4.0),
      (2, 1, 2.5), (2, 3, 3.0), (2, 4, 6.0), (2, 5, 3.0),
      (3, 1, 1.5), (3, 2, 2.0), (3, 4, 5.0), (3, 5, 5.0),
      (4, 1, 7.0), (4, 2, 7.0), (4, 3, 3.0), (4, 5, 3.0),
      (5, 1, 4.0), (5, 2, 2.0), (5, 3, 2.0), (5, 4, 1.0)
    )
  ).toDF("src", "dst", "cost")

  val graph = GraphFrame(vertex, edges)

  // Ciclos de tamanho 1
  graph.find("(a)-[e]->(b)").show()

  // Ciclos de tamanho 2
  graph.find("(a)-[e1]->(b); (b)-[e2]->(c)").filter("e1.src = e2.dst")show()

  // Ciclos de tamanho 3
  graph.find("(a)-[e1]->(b); (b)-[e2]->(c); (c)-[e3]->(d)").filter("e1.src = e3.dst").show()

  graph.find("(a)-[e]->(b)").selectExpr("a.id as a_id", "b.id as b_id").collect().foreach { row =>
    val a = row.getInt(0)
    val b = row.getInt(1)

    println(s"$a -> $b")
  }

  graph.find("(a)-[e]->(b)").filter("a.id != b.id").d
}
