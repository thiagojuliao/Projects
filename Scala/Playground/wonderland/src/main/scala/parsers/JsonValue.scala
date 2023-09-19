package parsers

export JsonValue.*

enum JsonValue:
  case JsonNull
  case JsonBool(bool: Boolean)
  case JsonNumber(n: Int)
  case JsonString(s: String)
  case JsonArray(elements: List[JsonValue])
  case JsonObject(obj: Map[String, JsonValue])
