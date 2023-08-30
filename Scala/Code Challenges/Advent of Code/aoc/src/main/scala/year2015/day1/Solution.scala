package year2015.day1

import utils.Ops
import scala.annotation.tailrec

object Solution extends App with Ops:
  private val instructions = loadInput(2015, 1)

  private def upOrDown(c: Char): Int =
    if c == '(' then 1 else -1

  def part1(instructions: String): Int =
    instructions.map(upOrDown).sum

  def part2(instructions: String): Int =
    @tailrec
    def loop(res: String, floor: Int, position: Int): Int =
      if floor == -1 then position
      else loop(res.tail, floor + upOrDown(res.head), position + 1)

    loop(instructions, 0, 0)

  def solve(): Unit =
    println(s"* Part 1: ${part1(instructions)}")
    println(s"* Part 2: ${part2(instructions)}")

  solve()
