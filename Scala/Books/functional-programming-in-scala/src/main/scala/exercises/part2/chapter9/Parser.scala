package exercises.part2.chapter9

import exercises.part2.chapter8.{Gen, Prop}
import exercises.part2.chapter9.Result.Success

import java.util.regex.Pattern
import scala.util.matching.Regex

type Parser[+A] = Location => Result[A]

object Parser:
  import Result.*

  /** Returns -1 if s1.startsWith(s2), otherwise returns the first index where the two strings differed. If s2 is longer than s1, returns s1.length.
    */
  def firstNonmatchingIndex(s1: String, s2: String, offset: Int): Int =
    var i = 0
    while i + offset < s1.length && i < s2.length do
      if s1.charAt(i + offset) != s2.charAt(i) then return i
      i += 1
    if s1.length - offset >= s2.length then -1
    else s1.length - offset

  def succeed[A](a: A): Parser[A] =
    _ => Success(a, 0)

  extension [A](p: Parser[A])
    /** 9.13: Implement string, regex, succeed, and slice for this initial representation of Parser. Note that slice is less efficient than it could
      * be, since it must still construct a value only to discard it. We’ll return to this later.
      */
    def string(s: String): Parser[String] =
      l =>
        val i = firstNonmatchingIndex(l.input, s, l.offset)
        if i == -1 then Success(s, s.length)
        else Failure(l.advanceBy(i).toError(s"1$s"), true)

    def regex(r: Regex): Parser[String] =
      l =>
        r.findPrefixOf(l.input.substring(l.offset)) match
          case Some(m) => Success(m, m.length)
          case None    => Failure(l.toError(s"regex $r"), true)

    def succeed(a: A): Parser[A] =
      _ => Success(a, 0)

    def slice: Parser[String] =
      l =>
        p(l) match
          case Success(_, n)     =>
            Success(l.input.substring(l.offset, l.offset + n), n)
          case f @ Failure(_, _) => f

    def scope(msg: String): Parser[A] =
      l => p(l).mapError(_.push(l, msg))

    def label(msg: String): Parser[A] =
      l => p(l).mapError(_.label(msg))

    def attempt: Parser[A] =
      l => p(l).uncommit

    def or(p2: => Parser[A]): Parser[A] =
      l =>
        p(l) match
          case Failure(_, false) => p2(l)
          case r                 => r

    def flatMap[B](f: A => Parser[B]): Parser[B] =
      l =>
        p(l) match
          case Success(a, n)     =>
            f(a)(l.advanceBy(n))
              .addCommit(n != 0)
              .advanceSuccess(n)
          case f @ Failure(_, _) => f
  end extension
end Parser

enum Result[+A]:
  case Success(get: A, charsConsumed: Int)
  case Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]

  def mapError(f: ParseError => ParseError): Result[A] = this match
    case Failure(e, c) => Failure(f(e), c)
    case _             => this

  def uncommit: Result[A] = this match
    case Failure(e, true) => Failure(e, false)
    case _                => this

  /** Listing 9.3 Using addCommit to make sure our parser is committed */
  def addCommit(isCommitted: Boolean): Result[A] = this match
    case Failure(e, c) => Failure(e, c || isCommitted)
    case _             => this

  def advanceSuccess(n: Int): Result[A] = this match
    case Success(a, m) => Success(a, n + m)
    case _             => this
end Result

trait Parsers[Parser[+_]]:
  def char(c: Char): Parser[Char] =
    string(c.toString).map(_.charAt(0))

  def string(s: String): Parser[String]

  def orString(s1: String, s2: String): Parser[String]

  def succeed[A](a: A): Parser[A] =
    string("").map(_ => a)

  /** 9.6: Using flatMap and any other combinators, write the context-sensitive parser we couldn’t express earlier. To parse the digits, you can make
    * use of a new primitive, regex, which promotes a regular expression to a Parser.10 In Scala, a string s can be promoted to a Regex object (which
    * has methods for matching) using s.r—for instance, "[a-zA-Z_][a-zA-Z0-9_]*".r:
    */
  def regex(r: Regex): Parser[String]

  def digits: Parser[String] =
    regex("\\d+".r)

  /** Parser which consumes zero or more whitespace characters. */
  def whitespace: Parser[String] = regex("\\s*".r)

  /** Parser which consumes reluctantly until it encounters the given string. */
  def thru(s: String): Parser[String] = regex((".*?" + Pattern.quote(s)).r)

  /** Unescaped string literals, like "foo" or "bar". */
  def quoted: Parser[String] = string("\"") *> thru("\"").map(_.dropRight(1))

  /** Unescaped or escaped string literals, like "An \n important \"Quotation\"" or "bar". */
  def escapedQuoted: Parser[String] =
    // rather annoying to write, left as an exercise
    // we'll just use quoted (unescaped literals) for now
    quoted.label("string literal").token

  /** C/Java style floating point literals, e.g .1, -1.0, 1e9, 1E-23, etc. Result is left as a string to keep full precision
    */
  def doubleString: Parser[String] =
    regex("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?".r).token

  /** Floating point literals, converted to a `Double`. */
  def double: Parser[Double] =
    doubleString.map(_.toDouble).label("double literal")

  /** A parser that succeeds when given empty input. */
  def eof: Parser[String] =
    regex("\\z".r).label("unexpected trailing characters")

  /** Listing 9.2 Combining Parser with map */
  object Laws:
    def equal[A](p1: Parser[A], p2: Parser[A])(in: Gen[String]): Prop =
      Prop.forAll(in)(s => p1.run(s) == p2.run(s))

    def mapLaw[A](p: Parser[A])(in: Gen[String]): Prop =
      equal(p, p.map(a => a))(in)

    /** 9.2: Hard: Try coming up with laws to specify the behavior of product. */

  extension [A](p: Parser[A])
    def run(input: String): Either[ParseError, A]

    def attempt: Parser[A]

    /** Listing 9.1 Adding infix syntax to parsers */
    infix def or(p2: => Parser[A]): Parser[A] // The infix modifier allows or to be used as an operator.
    def |(p2: => Parser[A]): Parser[A] = p or p2

    /** 9.4: Hard: Using map2 and succeed, implement the listOfN combinator from earlier: */
    def listOfN(n: Int): Parser[List[A]] =
      if n <= 0 then succeed(Nil)
      else p.map2(p.listOfN(n - 1))(_ :: _)

    /** 9.2 A possible algebra */
    /** 9.3: Hard: Before continuing, see if you can define many in terms of |, map2, and succeed. */
    def many: Parser[List[A]] =
      p.map2(p.many)(_ :: _) | succeed(Nil)

    /** 9.8: map is no longer primitive. Express it in terms of flatMap and/or other combinators. */
    def map[B](f: A => B): Parser[B] =
      p.flatMap(a => succeed(f(a)))

    /** 9.2.1 Slicing and nonempty repetition */
    def slice: Parser[String]

    def many1: Parser[List[A]] =
      p.map2(p.many)(_ :: _)

    /** 9.7: Implement product and map2 in terms of flatMap. */
    def product[B](p2: => Parser[B]): Parser[(A, B)] =
      p.flatMap(a => p2.map(b => (a, b)))

    def **[B](p2: => Parser[B]): Parser[(A, B)] = product(p2)

    /** 9.1: Using product, implement the now-familiar combinator map2, and then use this to implement many1 in terms of many. Note that we could have
      * chosen to make map2 primitive and defined product in terms of map2, as we’ve done in previous chapters.
      */
    def map2[B, C](p2: => Parser[B])(f: (A, B) => C): Parser[C] =
      p.flatMap(a => p2.map(b => f(a, b)))

    /** 9.3 Handling context sensitivity */
    def flatMap[B](f: A => Parser[B]): Parser[B]

    /** Sequences two parsers, ignoring the result of the first. We wrap the ignored half in slice, since we don't care about its result.
      */
    def *>[B](p2: => Parser[B]): Parser[B] =
      p.slice.map2(p2)((_, b) => b)

    /** Sequences two parsers, ignoring the result of the second. We wrap the ignored half in slice, since we don't care about its result.
      */
    def <*(p2: => Parser[Any]): Parser[A] =
      p.map2(p2.slice)((a, _) => a)

    /** Attempts `p` and strips trailing whitespace, usually used for the tokens of a grammar. */
    def token: Parser[A] = p.attempt <* whitespace

    /** Zero or more repetitions of `p`, separated by `p2`, whose results are ignored. */
    def sep(separator: Parser[Any]): Parser[List[A]] = // use `Parser[Any]` since don't care about result type of separator
      p.sep1(separator) | succeed(Nil)

    /** One or more repetitions of `p`, separated by `p2`, whose results are ignored. */
    def sep1(separator: Parser[Any]): Parser[List[A]] =
      p.map2((separator *> p).many)(_ :: _)

    def as[B](b: B): Parser[B] = p.slice.map(_ => b)

    /** Parses a sequence of left-associative binary operators with the same precedence. */
    def opL(op: Parser[(A, A) => A]): Parser[A] =
      p.map2((op ** p).many)((h, t) => t.foldLeft(h)((a, b) => b._1(a, b._2)))

    /** The root of the grammar, expects no further input following `p`. */
    def root: Parser[A] =
      p <* eof

    def label(msg: String): Parser[A]

    def scope(msg: String): Parser[A]
  end extension
end Parsers

/** 9.5 Error Reporting */
case class Location(input: String, offset: Int = 0):
  lazy val line: Int = input.slice(0, offset + 1).count(_ == '\n') + 1

  lazy val col: Int = input.slice(0, offset + 1).lastIndexOf('\n') match
    case -1        => offset + 1
    case lineStart => offset - lineStart

  def toError(msg: String): ParseError =
    ParseError(List((this, msg)))

  def advanceBy(n: Int): Location =
    copy(offset = offset + n)

case class ParseError(stack: List[(Location, String)] = Nil):
  def push(loc: Location, msg: String): ParseError =
    copy(stack = (loc, msg) :: stack)

  def label(s: String): ParseError =
    ParseError(latestLoc.map((_, s)).toList)

  def latestLoc: Option[Location] =
    latest.map(_(0))

  def latest: Option[(Location, String)] =
    stack.lastOption
