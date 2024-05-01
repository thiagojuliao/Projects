package year2016.day8

import utils.*

object Solution extends App with Ops:
  val input  = loadMultilineInput(2016, 8)
  val sample = loadMultilineInput(2016, 8, "sample.txt")

  def part1(screen: Screen, input: List[String]): Int =
    val instructions = input.map(Instructions.parse)
    screen.updateWith(instructions).pixels.count(_.on)

  def part2(screen: Screen, input: List[String]): String =
    val instructions = input.map(Instructions.parse)
    screen.updateWith(instructions).toString

  def solve(screen: Screen, input: List[String]): Unit =
    println(s"• Part 1: ${part1(screen, input)}")
    println(s"• Part 2: \n${part2(screen, input)}") // EFEYKFRFIJ

  solve(Screen.make(), input)
