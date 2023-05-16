package core

import implicits._
import org.apache.spark.internal.Logging
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.graphframes.GraphFrame
import utils.BuilderUtils

import scala.annotation.tailrec

object FloriaGriffiths extends Logging {

  def fromFile(filePath: String): FloriaGriffiths = {
    log.info(s"Building graph frame from file: $filePath")

    val (vertices, edges) = BuilderUtils.buildFromFile(filePath)

    new FloriaGriffiths(vertices, edges)
  }
}

case class FloriaGriffiths(private val vertices: DataFrame, private val edges: DataFrame) extends Logging {
  require(SparkSession.getActiveSession.nonEmpty)

  private val spark = SparkSession.getActiveSession.get
  import spark.implicits._

  val graph: GraphFrame = GraphFrame(vertices, edges).cache()

  // Initialization of start inputs
  private val V0 = graph.vertices
  private val U0 = graph.vertices.withColumn("u_value", lit(0.0))
  private val mc0 = graph.edges.select(max("cost")).collect()(0).getDouble(0)

  // Final results
  var u: DataFrame = spark.emptyDataFrame
  var mc: Option[Double] = None

  // Functions to compute the operators
  private def getTc_atIterationN(U: DataFrame, mc_ : Double): DataFrame =
    graph.edges.join(U, $"src" === $"id").drop("id")
      .groupBy($"dst".alias("id"))
      .agg(
        min($"u_value" + $"cost" - lit(mc_)).alias("tc_value")
      )

  private def getU_atIterationN(U: DataFrame, Tc: DataFrame): DataFrame =
    U.join(Tc, "id").withColumn("u_value", least($"u_value", $"tc_value")).drop("tc_value")

  private def getV_atIterationN(U: DataFrame, Tc: DataFrame): DataFrame =
    U.join(Tc, "id").filter($"u_value" > $"tc_value").select("id")

  private def getMC_atIterationN(mc_ : Double, size: Int): Double = {
    val motif = buildMotifPattern(size)

    val maybeMC = graph.find(motif)
      .filter(s"e1.src = e$size.dst")
      .groupBy()
      .agg(
        min(buildMinCPExpression(size))
      )
      .collect()(0).getDouble(0)

    Math.min(mc_, maybeMC)
  }

  private def printFinalResults(): Unit = {
    val u_ = u.collect().up

    log.info("Printing final results" +
      s"""
        |###########################################
        |#               Final Results             #
        |###########################################
        |• Corrector vector (u): $u_
        |• Cyclical minimal constant (mc): ${mc.get}
        |""".stripMargin
    )
  }

  // Main function to run the algorithm
  def run(cacheFinalResults: Boolean = true): Unit = {
    log.info("Floria-Griffiths algorithm start inputs:")
    
    debug(0, V0, U0, mc0)
    
    @tailrec
    def iterate(V: DataFrame, U: DataFrame, mc_ : Double, iter: Int = 1): Unit = {
      log.info(s"Processing iteration $iter...")

      if (V.isEmpty) {
        log.info("Processing finished successfully!")

        if (cacheFinalResults) {
          u = U.cache()
          u.count()
        }

        mc = Some(mc_)

        printFinalResults()
      } else {
        val Tc = getTc_atIterationN(U, mc_)
        val U_ = getU_atIterationN(U, Tc)
        val V_ = getV_atIterationN(U, Tc)
        val _mc_ = if (iter == 1) mc_ else getMC_atIterationN(mc_, iter)

        iterate(V_, U_, _mc_, iter + 1)
      }
    }

    iterate(V0, U0, mc0)
  }

  // Clears out any cached data and the computed cyclical minimal constant
  def clear(): Unit = {
    u.unpersist()
    graph.unpersist()
    mc = None
  }
}
