package core.implicits

import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.exchange._

case class ExchangeProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case ex: BroadcastExchangeExec => Map(
      "mode" -> ex.mode.getClass.getSimpleName
    )

    case ex: ShuffleExchangeExec => Map(
      "outputPartitioning" -> ex.outputPartitioning.toString,
      "shuffleOrigin" -> ex.shuffleOrigin.toString
    )

    case _ => Map.empty
  }
}
