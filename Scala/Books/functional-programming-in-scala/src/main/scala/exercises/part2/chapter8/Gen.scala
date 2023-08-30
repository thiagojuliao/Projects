package exercises.part2.chapter8

import exercises.part1.chapter6.RNG
import exercises.part1.chapter6.RNG.*
import exercises.part1.chapter6.State

import scala.annotation.tailrec

/** 8.1.4 The meaning and API of generators */
opaque type Gen[+A] = State[RNG, A]

/** 8.2 Test case minimization */
opaque type SGen[+A] = Int => Gen[A]
extension [A](self: SGen[A])
  def apply(n: Int): Gen[A] = self(n)

  /** 8.11: Implement map and flatMap for SGEN */
  def map[B](f: A => B): SGen[B] =
    n => self(n).map(f)

  def flatMap[B](f: A => SGen[B]): SGen[B] =
    n => self(n).flatMap(a => f(a)(n))

object Gen:

  /** 8.4: Implement Gen.choose using this representation of Gen. It should generate integers in the range start to stopExclusive. Feel free to use
    * functions you’ve already written:
    */
  def choose(start: Int, stopExclusive: Int): Gen[Int] =
    State(RNG.from(start, stopExclusive))

  /** 8.5: Let’s see what else we can implement using this representation of Gen. Try implementing unit, boolean, and listOfN.
    */
  def unit[A](a: => A): Gen[A] =
    State.unit(a)

  def boolean: Gen[Boolean] =
    State(RNG.boolean)

  def double: Gen[Double] =
    State(RNG.double)

  extension [A](self: Gen[A])
    def run(rng: RNG): (A, RNG) =
      self.run(rng)

    def listOfN(n: Int): Gen[List[A]] = State { rng =>
      @tailrec
      def generate(count: Int, state: RNG, res: List[A]): (List[A], RNG) =
        if count == 0 then res -> state
        else
          val (a, state_) = self.run(state)
          generate(count - 1, state_, a :: res)

      generate(n, rng, Nil)
    }

    def listOfN2(n: Int): Gen[List[A]] =
      State.sequence(List.fill(n)(self))

    /** 8.6: Implement flatMap, and then use it to implement this more dynamic version of listOfN. Put flatMap and listOfN in the Gen class:
      */
    def flatMap[B](f: A => Gen[B]): Gen[B] = State { rng =>
      val (a, rng_) = self.run(rng)
      f(a).run(rng_)
    }

    def map[B](f: A => B): Gen[B] =
      self.flatMap(a => unit(f(a)))

    def map2[B, C](gb: Gen[B])(f: (A, B) => C): Gen[C] =
      self.flatMap(a => gb.map(b => f(a, b)))

    def listOfN(size: Gen[Int]): Gen[List[A]] =
      size.flatMap(n => listOfN(n))

    @annotation.targetName("product")
    def **[B](gb: Gen[B]): Gen[(A, B)] =
      map2(gb)((_, _))

    /** 8.10: Implement a helper function for converting a Gen to an SGen, which ignores the size parameter. You can add this as an extension method
      * on Gen:
      */
    def unsized: SGen[A] = _ => self

    /** 8.12: Implement a list combinator that doesn’t accept an explicit size. It should return an SGen instead of a Gen. The implementation should
      * generate lists of the requested size:
      */
    def list: SGen[List[A]] = n => self.listOfN(n)

    /** 8.13: Define nonEmptyList for generating nonempty lists, and then update your specification of max to use this generator.
      */
    def nonEmptyList: SGen[List[A]] = n => self.listOfN(1 max n)
  end extension

  /** 8.7: Implement union, for combining two generators of the same type into one, by pulling values from each generator with equal likelihood:
    */
  def union[A](g1: Gen[A], g2: Gen[A]): Gen[A] =
    Gen.boolean.flatMap(b => if b then g1 else g2)

  /** 8.8: Implement weighted, a version of union that accepts a weight for each Gen and generates values from each Gen with probability proportional
    * to its weight:
    */
  def weighted[A](g1: (Gen[A], Double), g2: (Gen[A], Double)): Gen[A] =
    val g1Threshold = g1(1).abs / (g1(1).abs + g2(1).abs)
    Gen.double.flatMap(d => if d < g1Threshold then g1(0) else g2(0))
