package exercises.part3.chapter11

export Functor.syntax.*

trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

object Functor:
  def apply[F[_]](using functor: Functor[F]): Functor[F] = functor

  object syntax:
    extension [F[_]: Functor, A](fa: F[A]) def map[B](f: A => B): F[B] = Functor[F].map(fa)(f)
