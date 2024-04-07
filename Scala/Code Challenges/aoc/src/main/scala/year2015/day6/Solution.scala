package year2015.day6

import utils.Ops
import Light.*

object Solution extends App with Ops:
  private val input = loadMultilineInput(2015, 6)

  def part1: Int =
    val grid = Grid.make(1000, 1000)(LightState.OFF)
    GridInstructions.applyAll(grid)(input).count(_.value.isOn)

  def part2: Int =
    val grid = Grid.make(1000, 1000)(Light.make)
    GridInstructions.applyAll(grid)(input).map(_.value.brightness).sum

  def solve(): Unit =
    println(s"* Part 1: $part1")
    println(s"* Part 2: $part2")

  solve()
