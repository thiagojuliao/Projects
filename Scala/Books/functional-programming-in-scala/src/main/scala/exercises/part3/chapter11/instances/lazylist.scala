package exercises.part3.chapter11.instances

import exercises.part3.chapter11.Monad

object lazylist:
  /** 11.1: Write monad instances for Option, List, LazyList, Par, and Parser. */
  given Monad[LazyList] with
    override def unit[A](a: => A): LazyList[A] = LazyList(a)

    override def flatMap[A, B](fa: LazyList[A])(f: A => LazyList[B]): LazyList[B] =
      fa.flatMap(f)