package core.implicits

import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.python._

case class PythonProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case py: BatchEvalPythonExec => Map(
      "udfs" -> py.udfs.map(_.sql).toList,
      "resultAttrs" -> py.resultAttrs.map(_.sql).toList
    )

    case _ => Map.empty
  }
}
