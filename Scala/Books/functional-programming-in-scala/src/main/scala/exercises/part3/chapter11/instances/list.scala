package exercises.part3.chapter11.instances

import exercises.part3.chapter11.Monad

object list:
  /** 11.1: Write monad instances for Option, List, LazyList, Par, and Parser. */
  given Monad[List] with
    override def unit[A](a: => A): List[A] = List(a)

    override def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] =
      fa.flatMap(f)