package year2015.day6

import utils.extensions.*

object GridInstructions:
  private val pattern = "(.+) (\\d+,\\d+) through (\\d+,\\d+)".r

  def parse[A](s: String)(using parser: CommandParser[A]): (Command[A], Range, Range) =
    s match
      case pattern(cmd, rng1, rng2) =>
        (parser.parse(cmd), Range.parse(rng1), Range.parse(rng2))

  def apply[A: CommandParser](state: Grid[A])(s: String): Grid[A] =
    val (cmd, (x1, y1), (x2, y2)) = parse(s)

    state.map { case cell @ Cell(x, y, _) =>
      if x.between(x1, x2) && y.between(y1, y2) then cell.modify(cmd)
      else cell
    }

  def applyAll[A: CommandParser](state: Grid[A])(ss: List[String]): Grid[A] =
    ss.foldLeft(state)((grid, cmd) => GridInstructions.apply(grid)(cmd))
