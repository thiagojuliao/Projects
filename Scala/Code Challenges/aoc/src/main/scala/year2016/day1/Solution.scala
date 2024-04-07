package year2016.day1

import utils.*
import scala.annotation.tailrec

enum Direction:
  case UP, RIGHT, DOWN, LEFT

object Direction:
  def parse(s: String): (Direction, Int) = s match
    case s"R$n" => RIGHT -> n.toInt
    case s"L$n" => LEFT  -> n.toInt
    case s"U$n" => UP    -> n.toInt
    case s"D$n" => DOWN  -> n.toInt

case class Vector2D(x: Int, y: Int, d: Direction):
  import Direction.*

  def turn(d: Direction, steps: Int): Vector2D = (this.d, d) match
    case (UP, RIGHT)    => Vector2D(x + steps, y, d)
    case (UP, LEFT)     => Vector2D(x - steps, y, d)
    case (UP, DOWN)     => Vector2D(x, y - steps, d)
    case (UP, UP)       => Vector2D(x, y + steps, d)
    case (DOWN, RIGHT)  => Vector2D(x - steps, y, LEFT)
    case (DOWN, LEFT)   => Vector2D(x + steps, y, RIGHT)
    case (DOWN, UP)     => Vector2D(x, y + steps, UP)
    case (DOWN, DOWN)   => Vector2D(x, y - steps, DOWN)
    case (RIGHT, RIGHT) => Vector2D(x, y - steps, DOWN)
    case (RIGHT, LEFT)  => Vector2D(x, y + steps, UP)
    case (LEFT, RIGHT)  => Vector2D(x, y + steps, UP)
    case (LEFT, LEFT)   => Vector2D(x, y - steps, DOWN)
    case _              => Vector2D(x, y, d)

  def followInstruction(ins: String): Vector2D =
    val (direction, steps) = Direction.parse(ins)
    turn(direction, steps)

  def followInstructions(ins: List[String]): Vector2D =
    ins.foldLeft(this)((acc, ins) => acc.followInstruction(ins))

  def getSteps(ins: List[String]): List[Vector2D] =
    ins.foldLeft(List(this))((acc, ins) => acc.head.followInstruction(ins) :: acc).reverse

  def taxiCabDist(v: Vector2D): Int =
    val X = Math.abs(x - v.x); val Y = Math.abs(y - v.y)
    X + Y

object Vector2D:
  type Segment = (Vector2D, Vector2D)

  def pointsBetween(v1: Vector2D, v2: Vector2D): Seq[(Int, Int)] =
    if v1.x == v2.x then
      for y <- Math.min(v1.y, v2.y) + 1 until Math.max(v1.y, v2.y)
      yield (v1.x, y)
    else
      for x <- Math.min(v1.x, v2.x) + 1 until Math.max(v1.x, v2.x)
      yield (x, v1.y)

object Solution extends App with Ops:
  import Direction.*
  import Vector2D.*

  val input   = loadInput(2016, 1)
  val sample1 = loadInput(2016, 1, "sample1.txt")
  val sample2 = loadInput(2016, 1, "sample2.txt")
  val sample3 = loadInput(2016, 1, "sample3.txt")
  val sample4 = loadInput(2016, 1, "sample4.txt")

  val origin: Vector2D = Vector2D(0, 0, UP)

  private def getSegments(vectors: List[Vector2D]): List[Segment] =
    @tailrec
    def loop(rem: List[Vector2D], res: List[Segment]): List[Segment] =
      if rem.length < 2 then res
      else loop(rem.tail, res :+ (rem.head, rem.tail.head))
    loop(vectors, List())

  private def getFirstIntersection(segments: List[Segment]): Option[Vector2D] =
    @tailrec
    def loop(rem: List[Segment], expanded: Set[(Int, Int)]): Option[(Int, Int)] =
      if rem.isEmpty then None
      else
        val (v1, v2) = rem.head
        val points   = Vector2D.pointsBetween(v1, v2)
        val maybeInt = points.toSet.intersect(expanded).headOption

        if maybeInt.isDefined then maybeInt
        else loop(rem.tail, points.toSet ++ expanded)
    loop(segments, Set()).map((x, y) => Vector2D(x, y, UP))

  def part1(): Int =
    val ins  = input.split(", ").toList
    val dest = origin.followInstructions(ins)
    origin.taxiCabDist(dest)

  def part2(): Option[Int] =
    val ins          = input.split(", ").toList
    val steps        = origin.getSteps(ins)
    val segments     = getSegments(steps)
    val intersection = getFirstIntersection(segments)
    intersection.map(origin.taxiCabDist)

  def solve(): Unit =
    println(s"• Part 1: ${part1()}")
    println(s"• Part 2: ${part2()}")

  solve()
