import implicits._
import org.apache.spark.internal.Logging
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.graphframes.GraphFrame

import java.io.{BufferedWriter, File, FileWriter}

package object core extends Logging {

  def debug(iter: Int, V: DataFrame, U: DataFrame, mc: Double, Tc: Option[DataFrame] = None): Unit = {
    val vs = V.collect().sorted.vp
    val us = U.collect().sorted.up
    val tcs = if (Tc.nonEmpty) Tc.get.collect().sorted.tp else "[]"

    log.info(s"[Iteration $iter][V$iter] " + vs)
    log.info(s"[Iteration $iter][U$iter] " + us)

    if (iter > 0) {
      log.info(s"[Iteration $iter][Tc${iter - 1}] " + tcs)
    }

    log.info(s"[Iteration $iter][mc$iter] $mc")
  }

  def buildMotifPattern(size: Int = 1): String = {
    require(size >= 1)

    (1 to size)
      .map(i => s"(v$i)-[e$i]->(v${i + 1})")
      .reduce(_ + "; " + _)
  }

  def buildMinCPExpression(size: Int = 1): Column = {
    require(size >= 1)

    val string = (1 to size)
      .map(i => s"e$i.cost")
      .mkString("(", " + ", ")") + s" / $size"

    expr(string)
  }

  /*
    Applies the results of the Floria-Griffiths algorithm on the respective input cost matrix
    transforming every c(i,j) E M using the following expression:
                c'(i,j) := c(i,j) + u(i) - u(j) - mc
    If c'(i,j) = 0 then we've an edge that belongs to a minimal cycle
   */
  def transformCostMatrix(costMatrix: DataFrame, u: DataFrame, mc: Option[Double]): DataFrame = {
    require(!costMatrix.isEmpty)
    require(!u.isEmpty)
    require(mc.nonEmpty)
    require(SparkSession.getActiveSession.nonEmpty)

    val spark = SparkSession.getActiveSession.get
    import spark.implicits._

    val u_i = u.selectExpr(
      "id as src",
      "u_value as u_value_i"
    )

    val u_j = u.selectExpr(
      "id as dst",
      "u_value as u_value_j"
    )

    val costMatrix_ = costMatrix.join(u_i, "src")
      .join(u_j, "dst")
      .withColumn("cost_", $"cost" + $"u_value_i" - $"u_value_j" - lit(mc.get))
      .selectExpr(
        "src",
        "dst",
        "cost_ as cost"
      )

    costMatrix_
  }

  def filterSubCyclesExpression(size: Int): String = {
    val vColumns = (1 to size + 1).map(i => s"v$i")

    (2 to size + 1).map { i =>
      val columnsToCheck = vColumns.filter(v => v != "v1" & v != s"v$i").map(_ + ".id")

      s"not array_contains(array(${columnsToCheck.mkString(", ")}), v$i.id)"
    }
    .reduce(_ + " and " + _)
  }

  def printAllMinimalCycles(costMatrix: DataFrame, filePath: String): Unit = {
    require(!costMatrix.isEmpty)
    require(filePath.nonEmpty)

    val file = new File(filePath)
    val bw = new BufferedWriter(new FileWriter(file))

    val maxSize = costMatrix.select(max("src")).collect()(0).getInt(0)
    val vertices = costMatrix.selectExpr("src as id").dropDuplicates()
    val graph = GraphFrame(vertices, costMatrix.filter("cost = 0")).cache()

    (2 to maxSize).foreach { size =>
      val vColumns = (1 to size + 1).map(i => col(s"v$i.id"))
      val motif = buildMotifPattern(size)
      val subCyclesFilter = filterSubCyclesExpression(size)

      val minCycles = graph.find(motif)
        .filter(s"e1.src = e$size.dst")
        .select(vColumns:_*)
        .filter(subCyclesFilter)

      log.info(s"[FileWriter] Printing minimal cycles of size $size")

      val header =
        s"""
           |#################################################################
           |                       Floria-Griffiths
           |                 Minimal cycles of size $size
           |#################################################################
           |""".stripMargin

      bw.write(header + "\n")

      minCycles.collect().foreach { row =>
        val v0 = row.getInt(0).toString
        val cycle = (0 until size).map(i => row.getInt(i).toString).reduce(_ + "->" + _) + s"->$v0"

        bw.write(cycle)
        bw.write("\n")
      }
    }

    bw.close()
    graph.unpersist()
  }
}
