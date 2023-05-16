package json

import dsl._
import spray.json._

package object implicits extends DefaultJsonProtocol {
  implicit class JPrettyPrinter[T](obj: T) {
    def js(implicit writer: JsonWriter[T]): String = obj.toJson.prettyPrint
    def jc(implicit writer: JsonWriter[T]): String =  obj.toJson.compactPrint
  }

  // Custom Json format for Map[String, Any] and Any
  implicit val anyWriter: AnyJsonWriter = new AnyJsonWriter {}
  implicit val mapStringAnyFormat: MapStringAnyFormat = new MapStringAnyFormat

  // Custom formats for QueryExecutionData and SparkLineageData
  implicit val sldFormat: RootJsonFormat[SparkLineageData] = jsonFormat5(SparkLineageData.apply)
  implicit val qedFormat: RootJsonFormat[QueryExecutionData] = jsonFormat10(QueryExecutionData.apply)
}
