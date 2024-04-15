package year2016.day6

import utils.*

object Solution extends App with Ops:
  val input  = loadMultilineInput(2016, 6)
  val sample = loadMultilineInput(2016, 6, "sample.txt")

  private def getErrorCorrectedMessage(messages: List[String]): String =
    val transposed = messages.map(_.toList).transpose
    transposed.map(_.groupBy(identity).view.mapValues(_.length).toMap.toList.maxBy(_._2)._1).mkString

  private def getErrorCorrectedMessage2(messages: List[String]): String =
    val transposed = messages.map(_.toList).transpose
    transposed.map(_.groupBy(identity).view.mapValues(_.length).toMap.toList.minBy(_._2)._1).mkString

  def part1(): String =
    getErrorCorrectedMessage(input)

  def part2(): String =
    getErrorCorrectedMessage2(input)

  def solve(): Unit =
    println(s"• Part 1: ${part1()}")
    println(s"• Part 2: ${part2()}")

  solve()
