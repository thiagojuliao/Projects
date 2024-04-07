package year2015.day10

import utils.Ops

object Solution extends App with Ops:
  private val input = loadInput(2015, 10)

  def lookAndSay(s: String): String =
    val sb = new StringBuilder
    var i  = 0

    while i < s.length do
      val c = s(i)

      val segmentLength = 1 + s.segmentLength(_ == c, i + 1)

      sb.append(segmentLength).append(c)
      i += segmentLength
    end while
    sb.toString

  def lookAndSayN(s: String, n: Int): Int =
    Iterator.iterate(s, n + 1)(lookAndSay).toList.reverse.head.length

  def solve(): Unit =
    println(s"* Part 1: ${lookAndSayN(input, 40)}")
    println(s"* Part 2: ${lookAndSayN(input, 50)}")

  solve()
