package exercises.part2.chapter8

import exercises.part2.chapter8.Gen.*
import exercises.part1.chapter6.{RNG, SimpleRNG}

/** 8.1.3 The meaning and API of properties */
trait Prop_ :
  self =>

  def check: Boolean

  /** 8.3: Assuming the following representation of Prop, implement && as a method of Prop: */
  def &&(that: Prop_): Prop_ = new Prop_ :
    override def check: Boolean = self.check && that.check

object Prop_ :
  opaque type SuccessCount = Int
  opaque type FailedCase   = String

/** 8.1.6 Refining the Prop data type */
opaque type TestCases = Int

object TestCases:
  extension (x: TestCases) def toInt: Int = x

  def fromInt(x: Int): TestCases = x

/** Listing 8.4 Generating test cases up to a given maximum size */
opaque type MaxSize = Int
object MaxSize:
  extension (x: MaxSize) def toInt: Int = x

  def fromInt(x: Int): MaxSize = x

import Prop.*
opaque type Prop = (MaxSize, TestCases, RNG) => Result

object Prop:
  import Result.*

  opaque type SuccessCount = Int
  extension (sc: SuccessCount) def +(n: Int): SuccessCount = sc + n

  opaque type FailedCase = String
  extension (fc: FailedCase) def +(s: String): FailedCase = fc + ";" + s

  /** Listing 8.3 Implementing forAll */
  def randomLazyList[A](g: Gen[A])(rng: RNG): LazyList[A] =
    LazyList.unfold(rng)(rng => Some(g.run(rng)))

  def buildMsg[A](s: A, e: Exception): String =
    s"test case: $s\n" +
      s"generated an exception: ${e.getMessage}\n" +
      s"stack trace:\n ${e.getStackTrace.mkString("\n")}"

  def forAll[A](as: Gen[A])(f: A => Boolean): Prop =
    (_, n, rng) =>
      randomLazyList(as)(rng)
        .zip(LazyList.from(0))
        .take(n)
        .map:
          case (a, i) =>
            try
              if f(a) then Passed
              else Falsified(a.toString, i)
            catch
              case e: Exception =>
                Falsified(buildMsg(a, e), i)
        .find(_.isFalsified)
        .getOrElse(Passed)

  @annotation.targetName("forAllSized")
  def forAll[A](g: SGen[A])(f: A => Boolean): Prop =
    (max, n, rng) =>
      val casesPerSize = (n - 1) / max + 1

      val props: LazyList[Prop] =
        LazyList
          .from(0)
          .take((n min max) + 1)
          .map(i => forAll(g(i))(f))

      val prop: Prop =
        props
          .map[Prop](p => (max, _, rng) => p(max, casesPerSize, rng))
          .toList
          .reduce(_ && _)
      prop(max, n, rng)

  /** A simple implementation of a verify primitive which construct a Prop that ignores the number of test cases
    */
  def verify(p: => Boolean): Prop =
    (_, _, _) => if p then Proved else Falsified("()", 0)

  /** 8.9: Now that we have a representation of Prop, implement && and || for composing Prop values. Notice that in the case of failure, we don’t know
    * which property was responsible— the left or right. Can you devise a way of handling this, perhaps by allowing Prop values to be assigned a tag
    * or label that gets displayed in the event of a failure?
    */
  extension (self: Prop)
    def &&(that: Prop): Prop =
      (max, n, rng) =>
        self(max, n, rng) match
          case Passed => that(max, n, rng)
          case x      => x

    def ||(that: Prop): Prop =
      (max, n, rng) =>
        self(max, n, rng) match
          case Falsified(_, _) => that(max, n, rng)
          case x               => x

    /** Listing 8.5 A check helper function for Prop */
    def check(
        maxSize: MaxSize = 100,
        testCases: TestCases = 100,
        rng: RNG = SimpleRNG(System.currentTimeMillis)
    ): Result =
      self(maxSize, testCases, rng)

    /** Listing 8.6 A run helper function for Prop */
    def run(maxSize: MaxSize = 100, testCases: TestCases = 100, rng: RNG = SimpleRNG(System.currentTimeMillis)): Unit =
      self(maxSize, testCases, rng) match
        case Falsified(msg, n) =>
          println(s"! Falsified after $n passed tests:\n $msg")

        case Passed =>
          println(s"+ OK, passed $testCases tests.")

        case Proved =>
          println(s"+ OK, proved property.")
  end extension

/** Listing 8.2 Creating a Result data type */
enum Result:
  case Passed
  case Falsified(failure: FailedCase, successes: SuccessCount)
  case Proved

  def isFalsified: Boolean = this match
    case Falsified(_, _) => true
    case _               => false
