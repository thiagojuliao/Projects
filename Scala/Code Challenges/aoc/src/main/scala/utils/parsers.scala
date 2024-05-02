package utils

import scala.annotation.tailrec

object parsers:
  def conditionalParser(predicate: String => Boolean): Parser[String] =
    Parser(s => if predicate(s) then s.tail -> s.head.toString else s -> "")

  val digitParser: Parser[String]  = conditionalParser(_.head.isDigit)
  val letterParser: Parser[String] = conditionalParser(_.head.isLetter)
  val symbolParser: Parser[String] = conditionalParser(s => !s.head.isDigit && !s.head.isLetter)

  def manyParser(parser: Parser[String]): Parser[String] = Parser { s =>
    @tailrec
    def loop(input: String, acc: String): (String, String) =
      lazy val (rem, a) = parser.run(input)

      if input.isEmpty || rem == input then input -> acc
      else loop(rem, acc + a)

    val (s0, init) = parser.run(s)
    loop(s0, init)
  }

  val numberParser: Parser[String] = manyParser(digitParser)
  val textParser: Parser[String]   = manyParser(letterParser)

  def alternativeParser(pa: Parser[String], pb: Parser[String]): Parser[String] = Parser { s =>
    val (s1, a) = pa.run(s); lazy val (s2, b) = pb.run(s1)
    if s1 != s then s1 -> a else s2 -> b
  }
end parsers

object Playground extends App:
  import parsers.*

  val aNumberString = "12345"
  println(digitParser.run(aNumberString))
  println(manyParser(digitParser).run(aNumberString))

  val digitAndLetterParser = alternativeParser(digitParser, letterParser)
  println(digitAndLetterParser.run("a12bc3"))
  println(manyParser(digitAndLetterParser).run("a12bc3"))
