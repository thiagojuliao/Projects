package exercises.part3.chapter12.instances

import exercises.part3.chapter12.Id
import exercises.part3.chapter12.Applicative

object id:
  given Applicative[Id] with
    override def unit[A](a: => A): Id[A] = a

    override def ap[A, B](a: Id[A])(f: Id[A => B]): Id[B] = f(a)
