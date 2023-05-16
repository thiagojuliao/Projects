package core.implicits

import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.window._

case class WindowProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case w: WindowExec => Map(
      "windowExpression" -> w.windowExpression.map(_.sql).toList,
      "partitionSpec" -> w.partitionSpec.map(_.sql).toList,
      "orderSpec" -> w.orderSpec.map(_.sql).toList
    )

    case _ => Map.empty
  }
}
