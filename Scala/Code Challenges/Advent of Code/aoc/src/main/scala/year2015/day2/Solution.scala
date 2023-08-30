package year2015.day2

import utils.Ops

object Solution extends App with Ops:
  private val input: List[Box] =
    loadMultilineInput(2015, 2).map(Box.buildFrom)

  case class Box(l: Int, w: Int, h: Int):
    private val measures: List[Int] = List(l, w, h)

    def surfaceArea: Int =
      2 * l * w + 2 * w * h + 2 * h * l

    def smallestSideArea: Int =
      l * w min l * h min w * h

    def volume: Int = l * w * h

    def sides: List[(Int, Int)] =
      measures.foldLeft((measures.tail, Nil: List[(Int, Int)])) {
        case (Nil, res) -> _ => Nil -> res

        case (bs, res) -> a =>
          val pairs = bs.map(b => (a, b))
          bs.tail -> (pairs ++ res)
      }._2

    def perimeters: List[Int] =
      sides.map((a, b) => 2 * (a + b))

  object Box:
    def buildFrom(s: String): Box =
      val tokens = s.split("x").map(_.toInt)
      Box(tokens(0), tokens(1), tokens(2))

    def squareFeetOfPaper(b: Box): Int =
      b.surfaceArea + b.smallestSideArea

  def part1(boxes: List[Box]): Int =
    boxes.map(Box.squareFeetOfPaper).sum

  def part2(boxes: List[Box]): Int =
    boxes.map(Ribbon.totalFeet).sum

  object Ribbon:
    def totalFeet(b: Box): Int =
      b.perimeters.min + b.volume

  def solve(): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve()