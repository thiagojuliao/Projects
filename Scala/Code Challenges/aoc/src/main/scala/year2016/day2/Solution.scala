package year2016.day2

import utils.*

import scala.annotation.tailrec

object Solution extends App with Ops:
  val input  = loadMultilineInput(2016, 2)
  val sample = loadMultilineInput(2016, 2, "sample.txt")

  private def getInstructions(input: String): List[String] =
    input.split("").toList

  private def getButtonAndPosition[T](kp: KeyPad[T], instructions: List[String], startPos: Position): (T, Position) =
    @tailrec
    def loop(inst: List[String], pos: Position): (T, Position) =
      if inst.isEmpty then kp.getButton(pos).get -> pos
      else
        val np = pos + Position.parse(inst.head)

        if kp.getButton(np).isDefined then loop(inst.tail, np)
        else loop(inst.tail, pos)
    loop(instructions, startPos)

  private def getBathroomCode[T](kp: KeyPad[T], instructions: List[String], startPos: Position = Position(1, 1)): String =
    instructions
      .foldLeft(("", startPos)) { case ((c, p), i) =>
        val (b, p_) = getButtonAndPosition(kp, getInstructions(i), p)
        (c + b) -> p_
      }
      ._1

  def part1(): String =
    getBathroomCode(KeyPad.simpleKeyPad, input)

  def part2(): String =
    val initialPosition = Position(2, 0)
    getBathroomCode(KeyPad.bathroomKeyPad, input, initialPosition)

  def solve(): Unit =
    println(s"• Part 1: ${part1()}")
    println(s"• Part 2: ${part2()}")

  solve()
