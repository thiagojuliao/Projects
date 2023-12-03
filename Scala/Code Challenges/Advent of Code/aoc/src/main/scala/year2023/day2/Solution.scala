package year2023.day2

import utils.Ops

object Solution extends App with Ops:
  val input = loadMultilineInput(2023, 2)

  def part1: Int =
    input
      .map(Game.buildFromString)
      .filter(_.isValid)
      .map(_.id)
      .sum

  def part2: Int =
    input
      .map(Game.buildFromString)
      .map(_.power)
      .sum

  println(s"Part 1: $part1")
  println(s"Part 2: $part2")
