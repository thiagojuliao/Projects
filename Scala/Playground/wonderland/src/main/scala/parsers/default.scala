package parsers

import algebra.Applicative
import algebra.Functor.syntax.*
import algebra.Applicative.syntax.*
import algebra.Alternative.syntax.*
import algebra.instances.parser.given

export default.*

object default:
  def charParser(c: Char): Parser[String] = Parser { s =>
    if s.isEmpty then None
    else if s.head == c then Some(s.tail -> c.toString)
    else None
  }

  def stringParser(string: String): Parser[String] =
    val charParsers = string.toList.map(charParser)
    Parser.sequence(charParsers).map(_.mkString)

  def spanParser(p: Char => Boolean): Parser[String] = Parser { s =>
    val (taken, remaining) = s.span(p)
    Some(remaining -> taken)
  }

  def notNullParser(pa: Parser[String]): Parser[String] = Parser { s1 =>
    pa.run(s1).flatMap((s2, a) => if a.isEmpty then None else Some(s2 -> a))
  }

  def numberParser: Parser[String] =
    spanParser(_.isDigit)

  def stringLiteralParser: Parser[String] =
    charParser('"') *> spanParser(_ != '"') <* charParser('"')

  def whiteSpaceParser: Parser[String] =
    spanParser(_.isSpaceChar)

  def sepByParser[A, B](sep: Parser[A], element: Parser[B]): Parser[List[B]] =
    Applicative[Parser].map2(element, Parser.many(sep *> element))((a, as) =>
      a :: as
    )
      <|> Applicative[Parser].pure(Nil: List[B])

  def arrayParser[A](element: Parser[A]): Parser[List[A]] =
    val sep      = whiteSpaceParser *> charParser(',') <* whiteSpaceParser
    val elements = sepByParser(sep, element)

    charParser('[') *> whiteSpaceParser *>
      elements
      <* whiteSpaceParser <* charParser(']')

  def keyValueParser[A](valueParser: Parser[A]): Parser[(String, A)] =
    val keyParser = stringLiteralParser <*
      (whiteSpaceParser *> charParser(':') <* whiteSpaceParser)
    Applicative[Parser].map2(keyParser, valueParser)((k, v) => (k, v))

  def mapParser[A](valueParser: Parser[A]): Parser[Map[String, A]] =
    val pairsSep = whiteSpaceParser *> charParser(',') <* whiteSpaceParser

    val pairsParser = sepByParser(pairsSep, keyValueParser(valueParser))
      .map { pairs =>
        pairs.foldLeft(Map[String, A]())((mp, kv) => mp + kv)
      }

    charParser('{') *> whiteSpaceParser *>
      pairsParser
      <* whiteSpaceParser <* charParser('}')
