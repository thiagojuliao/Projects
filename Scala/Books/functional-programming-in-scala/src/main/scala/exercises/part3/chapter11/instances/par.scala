package exercises.part3.chapter11.instances

import exercises.part2.chapter7.Par
import exercises.part2.chapter7.Par.Par
import exercises.part3.chapter11.Monad

object par:
  /** 11.1: Write monad instances for Option, List, LazyList, Par, and Parser. */
  given Monad[Par] with
    override def unit[A](a: => A): Par[A] = Par.unit(a)

    override def flatMap[A, B](fa: Par[A])(f: A => Par[B]): Par[B] =
      fa.flatMap(f)