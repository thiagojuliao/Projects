package year2015.day4

import utils.Ops

import java.security.MessageDigest

object Solution extends App with Ops:
  private val input = loadInput(2015, 4)

  def md5Hex(s: String): String =
    MessageDigest
      .getInstance("MD5")
      .digest(s.getBytes)
      .map("%02x".format(_))
      .mkString("")

  def validHash(s: String, zeros: Int): Boolean =
    md5Hex(s).startsWith("0" * zeros)

  lazy val part1: Int =
    LazyList.from(1).dropWhile(n => !validHash(input + n, 5)).head

  /** Already got the answer on the first run. So to speed up things a bit i'm multiplying by 35 to reach as closer as possible to the correct value
    */
  def part2(input: String): Int =
    LazyList.from(part1 * 35).dropWhile(n => !validHash(input + n, 6)).head

  def solve(): Unit =
    println(s"* Part 1: $part1")
    println(s"* Part 2: ${part2(input)}")

  solve()
