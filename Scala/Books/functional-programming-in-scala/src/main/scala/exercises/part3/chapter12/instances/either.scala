package exercises.part3.chapter12.instances

import exercises.part3.chapter11.Monad

object either:
  /** 12.5: Write a monad instance for Either. */
  given [E]: Monad[[A] =>> Either[E, A]] with
    override def unit[A](a: => A): Either[E, A] = Right(a)

    override def flatMap[A, B](fa: Either[E, A])(f: A => Either[E, B]): Either[E, B] =
      fa.flatMap(f)
