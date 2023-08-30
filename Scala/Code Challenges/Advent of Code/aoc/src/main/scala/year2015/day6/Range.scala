package year2015.day6

type Range = (Int, Int)

object Range:
  private val pattern = "(\\d+),(\\d+)".r

  def parse(s: String): Range = s match
    case pattern(x, y) => (x.toInt, y.toInt)
