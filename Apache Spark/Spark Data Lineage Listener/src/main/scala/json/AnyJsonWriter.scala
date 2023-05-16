package json

import spray.json._

trait AnyJsonWriter extends JsonWriter[Any] {
  def write(obj: Any): JsValue = obj match {
    case n: Byte => JsNumber(n)
    case n: Short => JsNumber(n)
    case n: Int => JsNumber(n)
    case n: Long => JsNumber(n)
    case n: BigInt => JsNumber(n)
    case n: Double => JsNumber(n)
    case n: Float => JsNumber(n)
    case n: BigDecimal => JsNumber(n)
    case b: Boolean => JsBoolean(b)
    case c: Char => JsNumber(c)
    case s: String => JsString(s)
    case seq: Seq[_] => JsArray(seq.map(write).toVector)
    case ls: List[_] => JsArray(ls.map(write).toVector)

    case op: Option[_] => op match {
      case Some(value) => write(value)
      case _ => JsNull
    }

    case mp: Map[String, Any] => JsObject {
      mp.map(kv => (kv._1, write(kv._2)))
    }

    case _ => JsString(obj.toString)
  }
}
