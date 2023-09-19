package parsers

import algebra.Alternative.syntax.*
import algebra.Functor.syntax.*
import algebra.instances.parser.given

object JsonParser:
  def jsonNullParser: Parser[JsonValue] =
    stringParser("null").map(_ => JsonNull)

  def jsonBoolParser: Parser[JsonValue] =
    (stringParser("true") <|> stringParser("false"))
      .map(s => JsonBool(s.toBoolean))

  def jsonNumberParser: Parser[JsonValue] =
    notNullParser(numberParser).map(s => JsonNumber(s.toInt))

  def jsonStringParser: Parser[JsonValue] =
    stringLiteralParser.map(JsonString(_))

  def jsonArrayParser: Parser[JsonValue] =
    arrayParser(jsonParser).map(ls => JsonArray(ls))

  def jsonObjectParser: Parser[JsonValue] =
    mapParser(jsonParser).map(mp => JsonObject(mp))

  def jsonParser: Parser[JsonValue] =
    jsonNullParser <|> jsonBoolParser <|> jsonNumberParser <|> jsonStringParser <|> jsonArrayParser <|> jsonObjectParser
