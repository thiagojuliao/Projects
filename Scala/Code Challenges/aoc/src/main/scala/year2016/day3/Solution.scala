package year2016.day3

import utils.*

object Solution extends App with Ops:
  val input   = loadMultilineInput(2016, 3)
  val sample  = loadInput(2016, 3, "sample.txt")
  val sample2 = loadMultilineInput(2016, 3, "sample2.txt")

  private def extract(s: String): (Int, Int, Int) =
    val pattern = "\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)".r

    s match
      case pattern(a, b, c) => (a.toInt, b.toInt, c.toInt)

  private def buildColumnsArray(input: List[String]): Array[Array[Int]] =
    val empty: Array[Array[Int]] = Array.ofDim(3, input.length)

    input
      .foldLeft((empty, 0)) { case ((arr, i), s) =>
        val (a, b, c) = extract(s)

        arr(0)(i) = a; arr(1)(i) = b; arr(2)(i) = c
        arr -> (i + 1)
      }
      ._1

  def part1(): Int =
    input.map(Triangle.parse).count(_.isDefined)

  def part2(): Int =
    buildColumnsArray(input)
      .map(_.grouped(3).map(_.mkString(" ")).map(Triangle.parse))
      .map(_.count(_.isDefined))
      .sum

  def solve(): Unit =
    println(s"• Part 1: ${part1()}")
    println(s"• Part 2: ${part2()}")

  solve()
