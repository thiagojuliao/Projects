package json

import spray.json._
import DefaultJsonProtocol._

class MapStringAnyFormat extends JsonFormat[Map[String, Any]]{

  private val anyWriter = new AnyJsonWriter {}
  private implicit val anyReader: AnyJsonReader = new AnyJsonReader {}

  override def write(obj: Map[String, Any]): JsValue = JsObject(obj.mapValues(anyWriter.write))
  override def read(json: JsValue): Map[String, Any] = json match {
    case x: JsObject => x.fields.map { field =>
      (JsString(field._1).convertTo[String], field._2.convertTo[Any])
    }
    case x => deserializationError("Expected Map as JsObject, but got " + x)
  }
}
