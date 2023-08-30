package algebra

trait Applicative[F[_]] extends Functor[F] {
  def pure[A](a: A): F[A]

  def app[A, B](fab: F[A => B])(fa: F[A]): F[B]

  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    app(pure(f))(fa)

  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    app(map(fa)(f.curried))(fb)

  def map3[A, B, C, D](fa: F[A], fb: F[B], fc: F[C])(f: (A, B, C) => D): F[D] =
    val g = (a: A, b: B) => f.curried(a)(b)

    app(map2(fa, fb)(g))(fc)
}

object Applicative {
  def apply[F[_]](using ev: Applicative[F]): Applicative[F] = ev
}
