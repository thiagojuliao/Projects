package core.implicits

import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.adaptive._

case class AdaptiveProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case a: AdaptiveSparkPlanExec => Map(
      "isSubquery" -> a.isSubquery,
      "supportsColumnar" -> a.supportsColumnar
    )

    case a: AQEShuffleReadExec => Map(
      "hasCoalescedPartition" -> a.hasCoalescedPartition,
      "hasSkewedPartition" -> a.hasSkewedPartition,
      "isLocalRead" -> a.isLocalRead,
      "isCoalescedRead" -> a.isCoalescedRead
    )

    case _ => Map.empty
  }
}
