package json

import spray.json._
import DefaultJsonProtocol._

trait AnyJsonReader extends JsonReader[Any] {
  override def read(json: JsValue): Any = json match {
    case JsNumber(n) => n
    case JsBoolean(b) => b
    case JsString(s) => s
    case JsArray(a) => a.map(read)
    case JsObject(obj) => obj.mapValues(read)
    case JsNull => None
    case _ => json.convertTo[String]
  }
}