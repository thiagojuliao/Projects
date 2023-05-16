package core.implicits

import org.apache.spark.sql.execution._

case class ExecutionProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case e: CoalesceExec => Map(
      "numPartitions" -> e.numPartitions
    )

    case e: LimitExec => Map(
      "limit" -> e.limit
    )

    // TODO: Every new command result must be mapped here when suitable
    case e: CommandResultExec => e.commandPhysicalPlan match {
      case c: SparkPlan => Map(
        "command" -> c.getClass.getSimpleName,
        "mapped" -> false
      )
    }

    case e: ExpandExec => Map(
      "projections" -> e.projections.flatten.map(_.sql).toList,
      "outputColumns" -> e.output.map(_.sql).toList
    )

    case e: FileSourceScanExec =>
      val table = e.tableIdentifier.map(_.table)
      val database = e.tableIdentifier.flatMap(_.database)

      Map(
        "table" -> table,
        "database" -> database,
        "location" -> e.metadata("Location"),
        "dataFilters" -> e.dataFilters.map(_.sql).toList,
        "partitionFilters" -> e.partitionFilters.map(_.sql).toList,
        "format" -> e.metadata("Format").toLowerCase
      )

    case e: FilterExec => Map(
      "condition" -> e.condition.sql
    )

    case e: GenerateExec => Map(
      "generator" -> e.generator.sql,
      "requiredChildOutput" -> e.requiredChildOutput.map(_.sql).toList,
      "outer" -> e.outer,
      "generatorOutput" -> e.generatorOutput.map(_.sql).toList
    )

    case e: LocalTableScanExec => Map(
      "columns" -> e.output.map(_.sql).toList
    )

    case e: ProjectExec => Map(
      "columns" -> e.projectList.map(_.sql).toList
    )

    case e: RangeExec => Map(
      "start" -> e.start,
      "end" -> e.end,
      "step" -> e.step,
      "numSlices" -> e.numSlices,
      "numElements" -> e.numElements,
      "isEmptyRange" -> e.isEmptyRange
    )

    case e: RDDScanExec => Map(
      "columns" -> e.output.map(_.sql).toList,
      "name" -> e.name,
      "numPartitions" -> e.outputPartitioning.numPartitions,
      "outputOrdering" -> e.outputOrdering.map(_.sql).toList
    )

    case e: RowDataSourceScanExec => Map(
      "columns" -> e.output.map(_.sql).toList,
      "requiredSchema" -> e.requiredSchema.sql,
      "filters" -> e.filters.map(_.references).toList,
      "handledFilters" -> e.handledFilters.map(_.references).toList,
      "groupByColumns" -> e.aggregation.map(_.groupByColumns()),
      "aggregations" -> e.aggregation.map(_.aggregateExpressions()),
      "sizeInBytes" -> e.relation.sizeInBytes,
      "table" -> e.tableIdentifier.map(_.table),
      "database" -> e.tableIdentifier.flatMap(_.database)
    )

    case e: SampleExec => Map(
      "lowerBound" -> e.lowerBound,
      "upperBound" -> e.upperBound,
      "seed" -> e.seed,
      "withReplacement" -> e.withReplacement
    )

    case e: SortExec => Map(
      "global" -> e.global,
      "sortOrder" -> e.sortOrder.map(_.sql).toList
    )

    case e: SubqueryExec => Map(
      "name" -> e.name,
      "maxNumRows" -> e.maxNumRows
    )

    case e: SubqueryBroadcastExec => Map(
      "name" -> e.name,
      "buildKeys" -> e.buildKeys.map(_.sql).toList
    )

    case e: SubqueryAdaptiveBroadcastExec => Map(
      "name" -> e.name,
      "buildKeys" -> e.buildKeys.map(_.sql).toList,
      "onlyInBroadcast" -> e.onlyInBroadcast
    )

    case _ => Map.empty
  }
}
