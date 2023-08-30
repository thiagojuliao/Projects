package year2015.day5

import utils.Ops
import scala.annotation.tailrec

object Solution extends App with Ops:
  private val input = loadMultilineInput(2015, 5)

  object NotSoNiceString:
    private val invalidStrings = List("ab", "cd", "pq", "xy")

    def vowelsRule(s: String): Boolean =
      s.count("aeiou" contains _) >= 3

    def doubleRule(s: String): Boolean =
      val pattern = "\\w*([a-z])\\1\\w*".r
      pattern.matches(s)

    def validStringsRule(s: String): Boolean =
      !invalidStrings.exists(s contains _)

    def isNice(s: String): Boolean =
      validStringsRule(s) && vowelsRule(s) && doubleRule(s)

  object VeryNiceString:
    extension (self: Char) def ++(that: Char): String = s"$self$that"

    def twiceRule(s: String): Boolean =
      @tailrec
      def getPairs(prev: Char, rest: List[Char], pairs: List[String]): List[String] =
        rest match
          case Nil         => pairs
          case curr :: Nil => (prev ++ curr) :: pairs
          case curr :: t   =>
            if prev == curr && curr == t.head then getPairs(t.head, t.tail, pairs)
            else getPairs(curr, t, (prev ++ curr) :: pairs)

      val pairs  = getPairs(s.head, s.tail.toList, Nil)
      val groups = pairs.groupBy(identity)
      groups.view.values.exists(_.length >= 2)

    def betweenRule(s: String): Boolean =
      val pattern = "\\w*(([a-z])[a-z]\\2)\\w*".r
      pattern.matches(s)

    def isNice(s: String): Boolean =
      twiceRule(s) && betweenRule(s)

  def part1: Int = input.count(NotSoNiceString.isNice)
  def part2: Int = input.count(VeryNiceString.isNice)

  def solve(): Unit =
    println(s"* Part 1: $part1")
    println(s"* Part 1: $part2")

  solve()
