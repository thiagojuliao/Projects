package data

import algebra.{Applicative, Functor, Monad}
import syntax.all.*

final case class Kleisli[F[_], A, B](run: A => F[B]) {
  def ask(using A: Applicative[F]): Kleisli[F, A, A] =
    Kleisli[F, A, A](a => a.pure)

  def compose[C](k: Kleisli[F, C, A])(using M: Monad[F]): Kleisli[F, C, B] =
    Kleisli[F, C, B](c => k.run(c).flatMap(run))

  def andThen[C](k: Kleisli[F, B, C])(using M: Monad[F]): Kleisli[F, A, C] =
    Kleisli[F, A, C](a => run(a).flatMap(k.run))

  def local[AA](f: AA => A): Kleisli[F, AA, B] = Kleisli(f.andThen(run))

  def map[C](f: B => C)(using F: Functor[F]): Kleisli[F, A, C] =
    Kleisli[F, A, C](a => F.map(run(a))(f))

  def flatMap[C](f: B => Kleisli[F, A, C])(using M: Monad[F]): Kleisli[F, A, C] =
    Kleisli[F, A, C](a => run(a).flatMap(b => f(b).run(a)))
}

object Kleisli {
  type Id[A]        = A
  type Reader[A, B] = Kleisli[Id, A, B]

  def apply[F[_], A](using A: Applicative[F]): Kleisli[F, A, A] =
    Kleisli[F, A, A](a => a.pure)

  def pure[A](a: A): Kleisli[Id, A, A] =
    Kleisli[Id, A, A](_ => a)
}
