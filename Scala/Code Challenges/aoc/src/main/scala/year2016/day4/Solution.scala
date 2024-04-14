package year2016.day4

import utils.*

object Solution extends App with Ops:
  val input  = loadMultilineInput(2016, 4)
  val sample = loadMultilineInput(2016, 4, "sample.txt")

  def part1(input: List[String]): Int =
    input.map(Room.parse).filter(_.isReal).map(_.sectorId).sum

  def part2(input: List[String]): Option[Int] =
    val storage = "northpole object storage"

    input
      .map(Room.parse)
      .filter(_.isReal)
      .filter(_.decryptedName == storage)
      .map(_.sectorId)
      .headOption

  def solve(): Unit =
    println(s"• Part 1: ${part1(input)}")
    println(s"• Part 2: ${part2(input)}")

  solve()
