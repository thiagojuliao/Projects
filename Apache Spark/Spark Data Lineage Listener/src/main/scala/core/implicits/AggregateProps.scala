package core.implicits

import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.aggregate.BaseAggregateExec

case class AggregateProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case a: BaseAggregateExec => Map(
      "groupByKeys" -> a.groupingExpressions.map(_.sql).toList,
      "aggregations" -> a.aggregateExpressions.map(_.sql).toList
    )
  }
}
