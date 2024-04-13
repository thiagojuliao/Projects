package year2016.day3

case class Triangle private (a: Int, b: Int, c: Int)

object Triangle:
  private val pattern = "\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)".r

  def isValid(a: Int, b: Int, c: Int): Boolean =
    (a + b) > c && (a + c) > b && (b + c) > a

  def parse(s: String): Option[Triangle] = s match
    case pattern(a, b, c) =>
      val a_ = a.toInt; val b_ = b.toInt; val c_ = c.toInt

      if isValid(a_, b_, c_) then Some(Triangle(a_, b_, c_)) else None

    case _ => None
