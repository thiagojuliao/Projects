package algebra

trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    flatMap(fa)(a => pure(f(a)))

  override def app[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    flatMap(fa)(a => flatMap(fab)(f => pure(f(a))))
}
