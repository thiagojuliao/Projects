package year2015.day12

import utils.Ops
import year2015.day12.JSONParser.*

object Solution extends App with Ops:
  private val input = loadInput(2015, 12, "input.json")

  def part1(input: String): Int =
    val numberPattern = "-?\\d+".r
    numberPattern.findAllIn(input).map(_.toInt).sum

  def part2(input: String): Int =
    def helper(json: Json): Int = json match
      case JNumber(value)  => value
      case JString(_)      => 0
      case JArray(values)  => values.map(helper).sum
      case JObject(values) => if values.contains(JString("red")) then 0 else values.map(helper).sum

    helper(parse(input)._1)

  def solve(input: String): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
