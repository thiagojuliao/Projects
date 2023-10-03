package exercises.part3.chapter12.instances

import exercises.part3.chapter12.{Applicative, Traverse}

object option:
  given Applicative[Option] with
    override def unit[A](a: => A): Option[A] = Option(a)

    override def ap[A, B](fa: Option[A])(fab: Option[A => B]): Option[B] =
      for
        f <- fab
        a <- fa
      yield f(a)

  /** 12.13: Write Traverse instances for List, Option, Tree, and [x] =>> Map[K, x]: */
  given Traverse[Option] with
    override def traverse[G[_]: Applicative, A, B](fa: Option[A])(f: A => G[B]): G[Option[B]] =
      fa match
        case Some(a) => Applicative[G].map(f(a))(Option(_))
        case None    => Applicative[G].unit(Option.empty)
