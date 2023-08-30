package algebra

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {
  def apply[F[_]](using ev: Functor[F]): Functor[F] = ev
}
