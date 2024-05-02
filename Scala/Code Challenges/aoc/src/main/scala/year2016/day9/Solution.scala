package year2016.day9

import utils.*
import scala.annotation.tailrec

object Solution extends App with Ops:
  import parsers.*
  import Marker.*

  val input   = loadInput(2016, 9)
  val sample  = loadInput(2016, 9, "sample.txt")
  val sample2 = loadInput(2016, 9, "sample2.txt")
  val sample3 = loadInput(2016, 9, "sample3.txt")

  private val parser: Parser[String] = alternativeParser(textParser, markerParser)

  def part1(input: String): Int =
    @tailrec
    def loop(s: String, res: Int): Int =
      if s.isEmpty then res
      else
        val (s2, s1) = parser.run(s)

        Marker.make(s1) match
          case Some(Marker(l, t, _)) =>
            loop(s2.drop(l), res + l * t)

          case _ => loop(s2, res + s1.length)
    loop(input, 0)

  def part2(input: String): BigInt =
    def deductAndFilter(markers: Seq[Marker], n: Int): Seq[Marker] =
      markers.map(_.deduct(n)).filter(_.isValid)

    def getMultiplier(markers: Seq[Marker]): Int =
      markers.map(_.times).product

    @tailrec
    def loop(s: String, markers: Seq[Marker], acc: BigInt): BigInt =
      if s.isEmpty then acc
      else
        val (s2, s1) = parser.run(s)

        Marker.make(s1) match
          case Some(m @ Marker(_, _, r)) =>
            loop(s2, m +: deductAndFilter(markers, r.length), acc)

          case None =>
            if markers.isEmpty then loop(s2, markers, acc + s1.length)
            else
              val s3 = s1.take(markers.head.length)
              val s4 = s1.drop(markers.head.length)

              loop(s4 + s2, deductAndFilter(markers, s3.length), acc + s3.length * getMultiplier(markers))
    loop(input, Seq(), 0)

  def solve(input: String): Unit =
    println(s"• Part 1: ${part1(input)}")
    println(s"• Part 2: ${part2(input)}")

  solve(input)
end Solution
