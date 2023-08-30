package year2015.day3

import utils.Ops

object Solution extends App with Ops:
  private val input = loadInput(2015, 3)

  enum Direction:
    case North, East, West, South

  object Direction:
    def parse(c: Char): Direction = c match
      case '>' => East
      case '^' => North
      case 'v' => South
      case '<' => West
      case _   => sys.error("can only parse <, >, ^, v")

  case class House(x: Int, y: Int)

  object House:
    import Direction.*

    def origin: House = House(0, 0)

    extension (self: House)
      def +(that: House): House =
        House(self.x + that.x, self.y + that.y)

      def +(dir: Direction): House = dir match
        case North => self + House(0, 1)
        case East  => self + House(1, 0)
        case West  => self + House(-1, 0)
        case South => self + House(0, -1)

  def part1(input: String): Int =
    val directions = input.map(Direction.parse)

    directions
      .foldLeft(List(House.origin))((houses, dir) => houses.head + dir :: houses)
      .toSet
      .size

  def part2(input: String): Int =
    val directions = input.map(Direction.parse)

    directions
      .foldLeft(List(House.origin, House.origin))((houses, dir) => houses.tail.head + dir :: houses)
      .toSet
      .size

  def solve(): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve()
