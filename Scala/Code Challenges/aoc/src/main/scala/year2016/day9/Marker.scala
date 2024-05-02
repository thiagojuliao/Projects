package year2016.day9

import utils.*

final case class Marker private (length: Int, times: Int, repr: String):
  def isValid: Boolean          = length > 0
  def deduct(n: Int): Marker    = this.copy(length = length - n)
  override def toString: String = repr
end Marker

object Marker:
  private val markerRegex = "(\\(\\d+x\\d+\\))(.*)".r

  val markerParser: Parser[String] = Parser {
    case markerRegex(marker, rem) => rem -> marker
    case s: String                => s   -> ""
  }

  def make(s: String): Option[Marker] = s match
    case repr @ s"(${l}x${t})" =>
      Some(Marker(l.toInt, t.toInt, repr))

    case _ => None
end Marker
