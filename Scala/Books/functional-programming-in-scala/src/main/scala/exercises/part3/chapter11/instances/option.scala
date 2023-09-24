package exercises.part3.chapter11.instances

import exercises.part3.chapter11.Monad

object option:
  /** 11.1: Write monad instances for Option, List, LazyList, Par, and Parser. */
  given Monad[Option] with
    override def unit[A](a: => A): Option[A] = Option(a)

    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
      fa.flatMap(f)
