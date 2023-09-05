package year2015.day11

import utils.Ops
import scala.annotation.tailrec

object Solution extends App with Ops:
  private val input = loadInput(2015, 11)

  object PasswordVerifier:
    def firstRule(s: String): Boolean =
      @tailrec
      def loop(idx: Int): Boolean =
        if idx >= s.length - 2 then false
        else
          val triplet = s.slice(idx, idx + 3).map(_.toInt)
          triplet.zip(triplet.tail).dropWhile((n1, n2) => n2 - n1 == 1).isEmpty ||
          loop(idx + 1)

      loop(0)

    def secondRule(s: String): Boolean =
      !(s.contains("i") || s.contains("o") || s.contains("l"))

    def thirdRule(s: String): Boolean =
      val groupPattern = "([a-z])\\1".r
      groupPattern.findAllIn(s).toList.length >= 2

    def isValid(s: String): Boolean =
      secondRule(s) && thirdRule(s) && firstRule(s)
  end PasswordVerifier

  object Password:
    def increment(s: String): String =
      @tailrec
      def loop(rem: String, increment: Boolean, res: String): String =
        if !increment then (res ++ rem).reverse
        else if rem.head == 'z' then loop(rem.tail, true, res + "a")
        else
          val newLetter = (rem.head + 1).toChar.toString
          loop(rem.tail, false, res + newLetter)

      loop(s.reverse, true, "")

    def next(s: String): String = nextN(s).head

    def nextN(s: String, n: Int = 1): List[String] =
      Iterator.iterate(s)(increment).filter(PasswordVerifier.isValid).take(n).toList
  end Password

  def tests(): Unit =
    import Password.next as pnext

    println(pnext("abcdefgh"))
    println(pnext("ghijklmn"))

  def part1(s: String): String =
    Password.next(s)

  def part2(s: String): String =
    Password.nextN(s, 2).reverse.head

  def solve(s: String): Unit =
    println(s"* Part 1: ${part1(s)}")
    println(s"* Part 2: ${part2(s)}")

  solve(input)
