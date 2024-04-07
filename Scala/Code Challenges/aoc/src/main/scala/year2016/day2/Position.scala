package year2016.day2

case class Position(x: Int, y: Int):
  def +(other: Position): Position =
    Position(x + other.x, y + other.y)

object Position:
  val origin: Position = Position(0, 0)

  def parse(inst: String): Position = inst match
    case "U" => Position(-1, 0)
    case "D" => Position(1, 0)
    case "R" => Position(0, 1)
    case "L" => Position(0, -1)
    case _   => origin
