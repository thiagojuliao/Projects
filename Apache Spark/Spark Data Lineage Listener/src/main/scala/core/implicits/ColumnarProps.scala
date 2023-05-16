package core.implicits

import org.apache.spark.sql.execution.columnar.InMemoryTableScanExec
import org.apache.spark.sql.execution.{FileSourceScanExec, SparkPlan}

case class ColumnarProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case c: InMemoryTableScanExec =>
      // TODO: Every relation should be mapped here
      val sources = c.relation.cacheBuilder.cachedPlan.collectLeaves().map {
        case s: FileSourceScanExec => ExecutionProps(s).props

        case s: SparkPlan => Map(
          "relation" -> s.getClass.getSimpleName,
          "mapped" -> false
        )
      }

      Map(
        "table" -> c.relation.cacheBuilder.tableName,
        "sources" -> sources.toList,
        "storageLevel" -> c.relation.cacheBuilder.storageLevel.description,
        "sizeInBytes" -> c.relation.cacheBuilder.sizeInBytesStats.value
      )

    case _ => Map.empty
  }
}
