package exercises.part3.chapter11.instances

import exercises.part3.chapter11.{Id, Monad}

object id:
  given Monad[Id] with
    override def unit[A](a: => A): Id[A] = Id(a)

    override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] =
      fa.flatMap(f)
