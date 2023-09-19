package algebra

trait Alternative[F[_]] extends Applicative[F]:
  def empty[A]: F[A]
  def orElse[A](fa1: F[A], fa2: => F[A]): F[A]
  def pure[A](a: A): F[A]
  def ap[A, B](fa: F[A])(fab: F[A => B]): F[B]

object Alternative:
  def apply[F[_]](using alternative: Alternative[F]): Alternative[F] =
    alternative

  object syntax:
    extension [F[_]: Alternative, A](fa: F[A])
      def <|>(fa2: => F[A]): F[A] = Alternative[F].orElse(fa, fa2)
