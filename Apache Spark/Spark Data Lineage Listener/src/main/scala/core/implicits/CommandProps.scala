package core.implicits

import io.delta.tables.execution.VacuumTableCommand
import org.apache.spark.sql.delta.commands._
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.command._
import org.apache.spark.sql.execution.datasources.InsertIntoHadoopFsRelationCommand

import scala.collection.JavaConverters._

case class CommandProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    // TODO: Every new data writing command should be pattern matched here
    case c: DataWritingCommandExec => c.cmd match {
      case cmd: InsertIntoHadoopFsRelationCommand =>
        val database = cmd.catalogTable.flatMap(_.identifier.database)

        Map(
          "command" -> cmd.getClass.getSimpleName,
          "outputPath" -> cmd.outputPath.toString,
          "fileFormat" -> cmd.fileFormat.toString,
          "mode" -> cmd.mode.toString,
          "outputColumnNames" -> cmd.outputColumnNames,
          "partitionColumns" -> cmd.partitionColumns.attrs.map(_.sql),
          "table" -> cmd.catalogTable.map(_.identifier.table),
          "database" -> database
        )

      // Others, not mapped
      case cmd: DataWritingCommand => Map(
        "command" -> cmd.getClass.getSimpleName,
        "mapped" -> false
      )
    }

    // TODO: Every new executed command should be pattern matched here
    case c: ExecutedCommandExec => c.cmd match {
        // Delta Table Commands
        case cmd: CreateDeltaTableCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.table.identifier.table,
          "database" -> cmd.table.identifier.database,
          "mode" -> cmd.mode.toString,
          "location" -> cmd.table.location.getPath,
          "columns" -> cmd.output.attrs.map(_.sql).toList
        )

        case cmd: VacuumTableCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "path" -> cmd.path.toString,
          "table" -> cmd.table.map(_.table),
          "database" -> cmd.table.flatMap(_.database),
          "horizonHours" -> cmd.horizonHours,
          "dryRun" -> cmd.dryRun
        )

        case cmd: UpdateCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "path" -> cmd.tahoeFileIndex.path.toString,
          "target" -> cmd.toString, // TODO: Links to a Logical Plan and must be pattern matched to extract table info
          "updateExpressions" -> cmd.updateExpressions.map(_.sql).toList,
          "condition" -> cmd.condition.map(_.sql)
        )

        case cmd: MergeIntoCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "source" -> cmd.source.toString, // TODO: Links to a Logical Plan and must be pattern matched to extract table info
          "target" -> cmd.target.toString, // TODO: Links to a Logical Plan and must be pattern matched to extract table info
          "path" -> cmd.targetFileIndex.path.toString,
          "matchedClauses" -> cmd.matchedClauses.map(_.sql).toList,
          "notMatchedClauses" -> cmd.notMatchedClauses.map(_.sql).toList,
          "migrateSchema" -> cmd.migratedSchema.map(_.sql)
        )

        case cmd: DeleteCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "dataPath" -> cmd.deltaLog.dataPath.toString,
          "condition" -> cmd.condition.map(_.sql)
        )

        case cmd: RestoreTableCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.sourceTable.catalogTable.map(_.identifier.table),
          "database" -> cmd.sourceTable.catalogTable.flatMap(_.identifier.database),
          "location" -> cmd.sourceTable.catalogTable.map(_.location.getPath),
          "cdcOptions" -> cmd.sourceTable.cdcOptions.asScala.toMap,
          "options" -> cmd.sourceTable.options,
          "version" -> cmd.sourceTable.timeTravelOpt.flatMap(_.version),
          "timestamp" -> cmd.sourceTable.timeTravelOpt.flatMap(_.timestamp)
        )

        case cmd: OptimizeTableCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.tableId.map(_.table),
          "dataset" -> cmd.tableId.flatMap(_.database),
          "path" -> cmd.path,
          "userPartitionPredicates" -> cmd.userPartitionPredicates.toList,
          "options" -> cmd.options
        )

        case cmd: DeltaGenerateCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.tableId.table,
          "database" -> cmd.tableId.database,
          "mode" -> cmd.modeName,
          "options" -> cmd.options
        )

        case cmd: ConvertToDeltaCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.tableIdentifier.table,
          "database" -> cmd.tableIdentifier.database,
          "partitionSchema" -> cmd.partitionSchema.map(_.sql),
          "deltaPath" -> cmd.deltaPath
        )

        case cmd: ShowTableColumnsCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.tableID.table.map(_.table),
          "database" -> cmd.tableID.database,
          "path" -> cmd.tableID.path
        )

        case cmd: DescribeDeltaDetailCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.tableIdentifier.map(_.table),
          "database" -> cmd.tableIdentifier.flatMap(_.database),
          "path" -> cmd.path,
          "hadoopConf" -> cmd.hadoopConf
        )

        case cmd: DescribeDeltaHistoryCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "table" -> cmd.tableIdentifier.map(_.table),
          "database" -> cmd.tableIdentifier.flatMap(_.database),
          "path" -> cmd.path,
          "limit" -> cmd.limit,
          "options" -> cmd.options
        )

        // Others, not mapped
        case cmd: LeafRunnableCommand => Map(
          "command" -> cmd.getClass.getSimpleName,
          "mapped" -> false
        )
      }

    case _ => Map.empty
  }
}
