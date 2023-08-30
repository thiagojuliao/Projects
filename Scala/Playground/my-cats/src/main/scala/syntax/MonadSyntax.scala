package syntax

import algebra.Monad

trait MonadSyntax {
  extension [F[_], A](fa: F[A])
    def map[B](f: A => B)(using ev: Monad[F]): F[B] =
      ev.map(fa)(f)

    def flatMap[B](f: A => F[B])(using ev: Monad[F]): F[B] =
      ev.flatMap(fa)(f)
}
