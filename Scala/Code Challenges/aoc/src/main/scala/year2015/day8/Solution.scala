package year2015.day8

import utils.Ops
import scala.annotation.tailrec

object Solution extends App with Ops:
  private val input       = loadMultilineInput(2015, 8)
  private val scaped      = "(\"?.*)(\\\\[\\\\\"])(.*\"?)".r
  private val hexadecimal = "(\"?.*)(\\\\x[0-9a-f]{2})(.*\"?)".r

  def computeMemorySize(s: String): Int =
    @tailrec
    def compute(cs: List[Char], res: String): Int = cs match
      case Nil     => res.length - 2
      case c :: cs =>
        res + c match
          case scaped(s1, _, s2)      => compute(cs, s1 + "*" + s2)
          case hexadecimal(s1, _, s2) => compute(cs, s1 + "*" + s2)
          case s                      => compute(cs, s)

    compute(s.toList, "")

  def computeEncoded(s: String): Int =
    @tailrec
    def compute(cs: List[Char], res: String): Int = cs match
      case Nil     => res.length + 4
      case c :: cs =>
        res + c match
          case scaped(s1, _, s2)      => compute(cs, s1 + "****" + s2)
          case hexadecimal(s1, _, s2) => compute(cs, s1 + "*****" + s2)
          case s                      => compute(cs, s)

    compute(s.toList, "")

  def diff1(s: String): Int =
    s.length - computeMemorySize(s)

  def diff2(s: String): Int =
    computeEncoded(s) - s.length

  def part1: Int =
    input.map(diff1).sum

  def part2: Int =
    input.map(diff2).sum

  def solve(): Unit =
    println(s"* Part 1: $part1")
    println(s"* Part 2: $part2")

  solve()
