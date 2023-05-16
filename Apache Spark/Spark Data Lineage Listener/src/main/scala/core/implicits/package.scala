package core

import org.apache.spark.sql.execution.SparkPlan

package object implicits {

  implicit class StringOps(s: String) {
    def toCamelCase: String = s.replace("-", "")
      .replace("_", "")
      .split(" ")
      .zipWithIndex
      .map {
        case (str, 0) => str
        case (str, _) => str.capitalize
      }
      .reduce(_ + _)
  }

  private[implicits] trait SparkPlanProps {
    val p: SparkPlan
    val props: Map[String, Any]
  }

  implicit class SparkPlanPropsExtractor(p: SparkPlan) {

    private final val adaptive = ".*org\\.apache\\.spark\\.sql\\.execution\\.adaptive.*".r
    private final val aggregate = ".*org\\.apache\\.spark\\.sql\\.execution\\.aggregate.*".r
    private final val columnar = ".*org\\.apache\\.spark\\.sql\\.execution\\.columnar.*".r
    private final val command = ".*org\\.apache\\.spark\\.sql\\.execution\\.command.*".r
    private final val datasources = ".*org\\.apache\\.spark\\.sql\\.execution\\.datasources.*".r
    private final val exchange = ".*org\\.apache\\.spark\\.sql\\.execution\\.exchange.*".r
    private final val joins = ".*org\\.apache\\.spark\\.sql\\.execution\\.joins.*".r
    private final val python = ".*org\\.apache\\.spark\\.sql\\.execution\\.python.*".r
    private final val window = ".*org\\.apache\\.spark\\.sql\\.execution\\.window.*".r
    private final val execution = ".*org\\.apache\\.spark\\.sql\\.execution.*".r

    def props: Map[String, Any] = p.getClass.toString match {
      case adaptive() => AdaptiveProps(p).props
      case aggregate() => AggregateProps(p).props
      case columnar() => ColumnarProps(p).props
      case command() => CommandProps(p).props
      case datasources() => DatasourcesProps(p).props
      case exchange() => ExchangeProps(p).props
      case joins() => JoinProps(p).props
      case python() => PythonProps(p).props
      case window() => WindowProps(p).props
      case execution() => ExecutionProps(p).props
      case _ => Map.empty
    }
  }
}
