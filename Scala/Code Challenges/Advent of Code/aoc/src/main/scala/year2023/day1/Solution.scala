package year2023.day1

import utils.Ops

object Solution extends App with Ops:
  val input: List[String] = loadMultilineInput(2023, 1)

  val spelledDigits: Map[String, String] = Map(
    "one"   -> "1",
    "two"   -> "2",
    "three" -> "3",
    "four"  -> "4",
    "five"  -> "5",
    "six"   -> "6",
    "seven" -> "7",
    "eight" -> "8",
    "nine"  -> "9"
  )

  def getDigits(string: String): Seq[String] =
    string.filter(_.isDigit).map(_.toString)

  def getDigitsV2(string: String): Seq[String] =
    @scala.annotation.tailrec
    def loop(input: String, word: String, digits: Seq[String]): Seq[String] =
      if input.isEmpty then digits
      else
        val head       = input.head
        val maybeDigit = word + head.toString
        val validKey   = spelledDigits.view.keys.find(maybeDigit.contains)

        if head.isDigit then loop(input.tail, "", digits :+ head.toString)
        else if validKey.isDefined then loop(input.tail, head.toString, digits :+ spelledDigits(validKey.get))
        else loop(input.tail, maybeDigit, digits)

    loop(string, "", Seq())

  def getCalibrationValue(string: String, extractor: String => Seq[String]): Int =
    val digits = extractor(string)
    (digits.head + digits.last).toInt

  def part1: Int =
    input.map(getCalibrationValue(_, getDigits)).sum

  def part2: Int =
    input.map(getCalibrationValue(_, getDigitsV2)).sum

  println(s"Part 1: $part1")
  println(s"Part 2: $part2")
