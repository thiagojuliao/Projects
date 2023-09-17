package parsers

import algebra.Functor.syntax.*
import algebra.Applicative.syntax.*
import algebra.instances.parser.given

export default.*

object default:
  def charParser(c: Char): Parser[Char] = Parser { s =>
    if s.head == c then Some(s.tail -> c) else None
  }

  def stringParser(string: String): Parser[String] =
    val charParsers = string.toList.map(charParser)
    Parser.sequence(charParsers).map(_.mkString)

  def spanParser(p: Char => Boolean): Parser[String] = Parser { s =>
    val (taken, remaining) = s.span(p)
    if taken.isEmpty then None else Some(remaining -> taken)
  }

  def numberParser: Parser[String] = spanParser(_.isDigit)

  def stringLiteralParser: Parser[String] =
    (charParser('"') *> spanParser(_ != '"')) <* charParser('"')

  def whiteSpaceParser: Parser[String] =
    spanParser(_.isSpaceChar)
