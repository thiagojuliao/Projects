package algebra

trait Functor[F[_]]:
  /**
   * "Penetrates" over a value into a context `F`, transforming its value by applying `f` while
   * still remaining within `F`.
   */
  def map[A, B](fa: F[A])(f: A => B): F[B]

object Functor:
  /**
   * Summons a given instance of `Functor[F]` into scope.
   */
  def apply[F[_]](using functor: Functor[F]): Functor[F] = functor

  object syntax:
    extension [F[_]: Functor, A](fa: F[A])
      def map[B](f: A => B): F[B] = Functor[F].map(fa)(f)
