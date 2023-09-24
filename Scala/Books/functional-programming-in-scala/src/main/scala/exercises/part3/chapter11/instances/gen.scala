package exercises.part3.chapter11.instances

import exercises.part2.chapter8.Gen
import exercises.part3.chapter11.Monad

object gen:
  /** 11.1: Write monad instances for Option, List, LazyList, Par, and Parser. */
  given Monad[Gen] with
    override def unit[A](a: => A): Gen[A] = Gen.unit(a)

    def flatMap[A, B](gen: Gen[A])(f: A => Gen[B]): Gen[B] =
      Gen.flatMap(gen)(f)