package parsers

export JsonValue.*

enum JsonValue:
  case JsonNull
  case JsonBool(bool: Boolean)
  case JsonNumber(n: Int)
  case JsonString(s: String)
  case JsonArray(elements: Array[JsonValue])
  case JsonObject(obj: Map[String, JsonValue])
