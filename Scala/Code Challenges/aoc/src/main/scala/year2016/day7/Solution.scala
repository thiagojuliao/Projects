package year2016.day7

import utils.*

import scala.annotation.tailrec

object Solution extends App with Ops:
  val input   = loadMultilineInput(2016, 7)
  val sample  = loadMultilineInput(2016, 7, "sample.txt")
  val sample2 = loadMultilineInput(2016, 7, "sample2.txt")

  private val ipv7Regex     = "(\\w+)|\\[\\w+\\]".r
  private val hypernetRegex = "\\[\\w+\\]".r
  private val abbaRegex     = "(\\w)(\\w)\\2\\1".r
  private val abaRegex      = "(\\w)(\\w)\\1".r

  @tailrec
  private def isABBA(s: String): Boolean =
    if s.length < 4 then false
    else abbaRegex.matches(s.take(4)) && s.take(4).distinct.length == 2 || isABBA(s.tail)

  private def supportsTLS(ip: String): Boolean =
    val fragments      = ipv7Regex.findAllIn(ip).toList
    val hypernets      = fragments.filter(hypernetRegex.matches).map { case s"[$s]" => s }
    lazy val supernets = fragments.filterNot(hypernetRegex.matches)

    hypernets.count(isABBA) == 0 && supernets.count(isABBA) > 0

  private def getABAs(s: String): List[String] =
    @tailrec
    def loop(input: String, acc: List[String]): List[String] =
      if input.length < 3 then acc
      else
        val frag = input.take(3)
        if abaRegex.matches(frag) && frag.distinct.length == 2 then loop(input.tail, frag :: acc)
        else loop(input.tail, acc)
    loop(s, List())

  private def getBABs(s: String): List[String] =
    getABAs(s).map(aba => s"${aba(1)}${aba(0)}${aba(1)}")

  private def supportsSSL(ip: String): Boolean =
    val fragments     = ipv7Regex.findAllIn(ip).toList
    val hypernets     = fragments.filter(hypernetRegex.matches).map { case s"[$s]" => s }
    val supernets     = fragments.filterNot(hypernetRegex.matches)
    val supernetsBABs = supernets.flatMap(getBABs)

    supernetsBABs.nonEmpty && supernetsBABs.count(bab => hypernets.count(_.contains(bab)) > 0) > 0

  def part1(input: List[String]): Int =
    input.count(supportsTLS)

  def part2(input: List[String]): Int =
    input.count(supportsSSL)

  def solve(input: List[String]): Unit =
    println(s"• Part 1: ${part1(input)}")
    println(s"• Part 2: ${part2(input)}")

  solve(input)
end Solution
