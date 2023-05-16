package core.implicits

import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.joins._

case class JoinProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case j: BroadcastHashJoinExec => Map(
      "leftKeys" -> j.leftKeys.map(_.sql).toList,
      "rightKeys" -> j.rightKeys.map(_.sql).toList,
      "joinType" -> j.joinType.sql,
      "buildSide" -> j.buildSide.toString,
      "condition" -> j.condition.map(_.sql),
      "isNullAwareAntiJoin" -> j.isNullAwareAntiJoin
    )

    case j: BroadcastNestedLoopJoinExec => Map(
      "leftKeys" -> j.leftKeys.map(_.sql).toList,
      "rightKeys" -> j.rightKeys.map(_.sql).toList,
      "joinType" -> j.joinType.sql,
      "buildSide" -> j.buildSide.toString,
      "condition" -> j.condition.map(_.sql)
    )

    case j: CartesianProductExec => Map(
      "leftKeys" -> j.leftKeys.map(_.sql).toList,
      "rightKeys" -> j.rightKeys.map(_.sql).toList,
      "joinType" -> j.joinType.sql,
      "condition" -> j.condition.map(_.sql)
    )

    case j: ShuffledHashJoinExec => Map(
      "leftKeys" -> j.leftKeys.map(_.sql).toList,
      "rightKeys" -> j.rightKeys.map(_.sql).toList,
      "joinType" -> j.joinType.sql,
      "buildSide" -> j.buildSide.toString,
      "condition" -> j.condition.map(_.sql),
      "isSkewJoin" -> j.isSkewJoin
    )

    case j: SortMergeJoinExec => Map(
      "leftKeys" -> j.leftKeys.map(_.sql).toList,
      "rightKeys" -> j.rightKeys.map(_.sql).toList,
      "joinType" -> j.joinType.sql,
      "condition" -> j.condition.map(_.sql),
      "isSkewJoin" -> j.isSkewJoin
    )

    case _ => Map.empty
  }
}
