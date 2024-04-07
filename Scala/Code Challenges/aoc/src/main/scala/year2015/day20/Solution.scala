package year2015.day20

import utils.Ops

object Solution extends App with Ops:
  private val input = loadInput(2015, 20).toInt

  /** Solved this challenge first using pyspark. See the .py file for the exact approach.
    */
  extension (n: Int)
    def divisors: List[Int] =
      Iterator.from(1).takeWhile(_ <= n).filter(n % _ == 0).toList

  def totalPresents(houseNumber: Int)(f: Int => Int): Int =
    f(houseNumber)

  val part1F: Int => Int = n => divisors(n).sum * 10
  val part2F: Int => Int = n => divisors(n).filter(n / _ < 50).sum * 11

  def part1(input: Int): Int =
    Iterator.from(786200).dropWhile(totalPresents(_)(part1F) <= input).take(1).toList.head

  def part2(input: Int): Int =
    Iterator.from(831550).dropWhile(totalPresents(_)(part2F) <= input).take(1).toList.head

  def solve(input: Int): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
