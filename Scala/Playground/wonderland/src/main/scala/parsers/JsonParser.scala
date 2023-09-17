package parsers

import algebra.Functor.syntax.*
import algebra.Alternative.syntax.*
import algebra.instances.parser.given

object JsonParser:
  val jsonNullParser: Parser[JsonValue] =
    stringParser("null").map(_ => JsonNull)

  val jsonBoolParser: Parser[JsonValue] =
    (stringParser("true") <|> stringParser("false"))
      .map(s => JsonBool(s.toBoolean))

  val jsonNumberParser: Parser[JsonValue] =
    numberParser.map(s => JsonNumber(s.toInt))

  val jsonStringParser: Parser[JsonValue] =
    stringLiteralParser.map(JsonString(_))

  val jsonParser: Parser[JsonValue] =
    jsonNullParser <|> jsonBoolParser <|> jsonNumberParser <|> jsonStringParser
