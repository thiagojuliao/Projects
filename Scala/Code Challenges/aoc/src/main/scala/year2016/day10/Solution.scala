package year2016.day10

import utils.*
import Factory.*

object Solution extends App with Ops:
  val input  = loadMultilineInput(2016, 10)
  val sample = loadMultilineInput(2016, 10, "sample.txt")

  val factory = Factory.create()

  def part1(instructions: List[String]): Int =
    factory.processAll(instructions).bots.view.values.filter(_.history.contains((17, 61))).head.id

  def part2(instructions: List[String]): Int =
    val output = factory.processAll(instructions).output
    output(0) * output(1) * output(2)

  def solve(instructions: List[String]): Unit =
    println(s"• Part 1: ${part1(instructions)}")
    println(s"• Part 2: ${part2(instructions)}")

  solve(input)
