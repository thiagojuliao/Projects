package core.implicits

import collection.JavaConverters._
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2._

case class DatasourcesProps(p: SparkPlan) extends SparkPlanProps {
  override val props: Map[String, Any] = p match {
    case d:  V2ExistingTableWriteExec => Map(
      "query" -> d.query.toString, // TODO: Links to a Spark Plan that must be pattern matched to extract table info
      "writeDescription" -> d.write.description
    )

    case d: AtomicCreateTableAsSelectExec => Map(
      "identifier" -> d.ident.toString,
      //"location" -> d.tableSpec.location,
      "partitioning" -> d.partitioning.map(_.describe()).toList,
      //"properties" -> d.tableSpec.properties,
      "properties" -> d.properties,
      "writeOptions" -> d.writeOptions.asScala.toMap,
      //"external" -> d.tableSpec.external
    )

    case d: CreateTableAsSelectExec => Map(
      "identifier" -> d.ident.toString,
      //"location" -> d.tableSpec.location,
      "partitioning" -> d.partitioning.map(_.describe()).toList,
      //"properties" -> d.tableSpec.properties,
      "properties" -> d.properties,
      "writeOptions" -> d.writeOptions.asScala.toMap,
      //"external" -> d.tableSpec.external
    )

    case d: CreateTableExec => Map(
      "identifier" -> d.identifier.toString,
      //"location" -> d.tableSpec.location,
      "partitioning" -> d.partitioning.map(_.describe()).toList,
      "properties" -> d.tableProperties,
      "schema" -> d.tableSchema.sql,
      //"external" -> d.tableSpec.external
    )

    case d: ReplaceTableAsSelectExec => Map(
      "identifier" -> d.ident.toString,
      //"location" -> d.tableSpec.location,
      "partitioning" -> d.partitioning.map(_.describe()).toList,
      //"properties" -> d.tableSpec.properties,
      "properties" -> d.properties,
      "writeOptions" -> d.writeOptions.asScala.toMap,
      //"external" -> d.tableSpec.external
    )

    case d: AtomicReplaceTableAsSelectExec => Map(
      "identifier" -> d.ident.toString,
      //"location" -> d.tableSpec.location,
      "partitioning" -> d.partitioning.map(_.describe()).toList,
      //"properties" -> d.tableSpec.properties,
      "properties" -> d.properties,
      "writeOptions" -> d.writeOptions.asScala.toMap,
      //"external" -> d.tableSpec.external
    )

    case _ => Map.empty
  }
}
