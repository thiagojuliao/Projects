package year2015.day7

import year2015.day7.Solution.Circuit
import year2015.day7.Solution.Circuit.*

import scala.annotation.tailrec

extension (s: String) def isNumber: Boolean = s.toCharArray.forall(_.isDigit)

object CircuitInstructions:
  private val setPattern    = "(\\w+) -> (\\w+)".r
  private val andPattern    = "(\\w+) AND (\\w+) -> (\\w+)".r
  private val orPattern     = "(\\w+) OR (\\w+) -> (\\w+)".r
  private val lShiftPattern = "(\\w+) LSHIFT (\\d+) -> (\\w+)".r
  private val rShiftPattern = "(\\w+) RSHIFT (\\w+) -> (\\w+)".r
  private val notPattern    = "NOT (\\w+) -> (\\w+)".r

  def apply(s: String)(c: Circuit): Either[String, Circuit] =
    s match
      case setPattern(w1, w2) =>
        if w1.isNumber then Right(c.set(w2, w1.toInt))
        else if c.contains(w1) then Right(c.set(w1, w2))
        else Left(s)

      case andPattern(w1, w2, w3) =>
        if w1.isNumber && c.contains(w2) then Right(c.and(w1.toInt, w2, w3))
        else if c.contains(w1, w2) then Right(c.and(w1, w2, w3))
        else Left(s)

      case orPattern(w1, w2, w3) =>
        if w1.isNumber && c.contains(w2) then Right(c.or(w1.toInt, w2, w3))
        else if c.contains(w1, w2) then Right(c.or(w1, w2, w3))
        else Left(s)

      case lShiftPattern(w1, n, w2) =>
        if c.contains(w1) then Right(c.lshift(w1, n.toInt, w2))
        else Left(s)

      case rShiftPattern(w1, n, w2) =>
        if c.contains(w1) then Right(c.rshift(w1, n.toInt, w2))
        else Left(s)

      case notPattern(w1, w2) =>
        if w1.isNumber then Right(c.not(w1.toInt, w2))
        else if c.contains(w1) then Right(c.not(w1, w2))
        else Left(s)

      case s => sys.error(s"invalid instruction: $s")

  def applyAll(instructions: List[String])(state: Circuit): Circuit =
    @tailrec
    def build(instructions: List[String], res: Circuit): Circuit =
      instructions match
        case Nil                => res
        case instruction :: rem =>
          apply(instruction)(res) match
            case Left(s)        => build(rem :+ s, res)
            case Right(circuit) => build(rem, circuit)
    build(instructions, state)
