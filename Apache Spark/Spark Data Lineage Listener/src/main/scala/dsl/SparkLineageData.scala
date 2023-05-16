package dsl

import core._
import core.implicits._
import org.apache.spark.sql.execution.metric.SQLMetricInfo

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

object SparkLineageData {

  private def buildMetrics(sqlMetrics: Seq[SQLMetricInfo], metricValues: Map[Long, String]): List[Map[String, Any]] = {
    val metrics = mutable.ListBuffer[Map[String, Any]]()

    sqlMetrics.foreach { smi =>
      val name = smi.name.toCamelCase
      val id = smi.accumulatorId
      val maybeValue = metricValues.get(id)

      if (maybeValue.nonEmpty) {
        val tokens = maybeValue.get.split(" ")
        val value = tokens(0).replace(",", "")
        val unit = if (tokens.size > 1) Some(tokens(1)) else None

        metrics += Map("name" -> name, "value" -> value, "unit" -> unit)
      }
    }

    metrics.toList
  }

  def apply(spi: SparkPlanInfo, metricValues: Map[Long, String], depth: Int = 0): List[SparkLineageData] = {
    val nodeId = new AtomicLong(0)

    def fromSparkPlanInfo(spi: SparkPlanInfo, metricValues: Map[Long, String], depth: Int = 0): List[SparkLineageData] = {
      val data = SparkLineageData(nodeId.getAndIncrement(), spi.nodeName, spi.props, buildMetrics(spi.metrics, metricValues), depth)

      if (spi.children.isEmpty) data :: Nil
      else data :: spi.children.toList.flatMap(fromSparkPlanInfo(_, metricValues, depth + 1))
    }

    fromSparkPlanInfo(spi, metricValues, depth)
  }
}

case class SparkLineageData(
                             nodeId: Long,
                             nodeName: String,
                             properties: Map[String, Any],
                             metrics: List[Map[String, Any]],
                             depth: Int
                           ) {
  override def toString: String = {
    val sb = new StringBuilder("")

    sb ++=
      s"""
         |***** [$depth] $nodeName @ $nodeId *****
         |""".stripMargin

    sb ++= s"|- properties:"

    properties.foreach { kv =>
      sb ++= s"\n|-|- ${kv._1}: ${kv._2.toString}"
    }

    sb ++= s"\n|- metrics:"

    metrics.foreach { mp =>
      val name = mp("name")
      val value = mp("value")
      val unit = mp("unit").asInstanceOf[Option[String]].getOrElse("")

      sb ++= s"\n|-|- $name: $value $unit".stripSuffix("")
    }

    sb.toString
  }
}
