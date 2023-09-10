package year2015.day17

import utils.Ops
import scala.annotation.tailrec

object Solution extends App with Ops:
  private val input  = loadMultilineInput(2015, 17).map(_.toInt)
  private val sample = loadMultilineInput(2015, 17, "sample.txt").map(_.toInt)

  def allCombinations[T](ls: List[T]): List[List[T]] =
    @tailrec
    def get(remaining: List[T], result: List[List[T]]): List[List[T]] =
      remaining match
        case Nil    => result
        case h :: t =>
          if result.isEmpty then get(t, List(h) :: result)
          else
            val newResult = List(h) :: result.flatMap(ls => List(h :: ls, ls))
            get(t, newResult)
    get(ls, Nil)

  def part1(input: List[Int]): Int =
    allCombinations(input).count(_.sum == 150)

  def part2(input: List[Int]): Int =
    val validContainers       = allCombinations(input).filter(_.sum == 150)
    val minValidContainerSize = validContainers.minBy(_.length).length
    validContainers.count(_.length == minValidContainerSize)

  def solve(input: List[Int]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
