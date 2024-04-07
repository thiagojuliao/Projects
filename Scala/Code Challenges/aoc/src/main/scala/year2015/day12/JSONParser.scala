package year2015.day12

import scala.util.matching.Regex

object JSONParser:
  sealed trait Json

  case class JNumber(value: Int) extends Json

  case class JString(value: String) extends Json

  case class JArray(values: Seq[Json]) extends Json

  case class JObject(values: Seq[Json]) extends Json

  val numberRegex: Regex = "(-?\\d+)(.*)".r
  val stringRegex: Regex = "\"(\\w+)\"(.*)".r

  def parse(input: String): (Json, String) = input match
    case numberRegex(digits, rest) => (JNumber(digits.toInt), rest)
    case stringRegex(string, rest) => (JString(string), rest)
    case _                         =>
      input.head match
        case '[' => parseArray(JArray(Seq()), input.tail)
        case '{' => parseObject(JObject(Seq()), input.tail)

  def parseArray(current: JArray, input: String): (Json, String) =
    val (value, remaining) = parse(input)
    val next               = JArray(current.values.appended(value))
    if remaining.head == ']' then (next, remaining.tail) else parseArray(next, remaining.tail)

  def parseObject(current: JObject, input: String): (Json, String) =
    val (_, first)         = parse(input)
    val (value, remaining) = parse(first.tail)
    val next               = JObject(current.values.appended(value))
    if remaining.head == '}' then (next, remaining.tail) else parseObject(next, remaining.tail)
end JSONParser
