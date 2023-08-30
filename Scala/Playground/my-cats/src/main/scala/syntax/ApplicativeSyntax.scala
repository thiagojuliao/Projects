package syntax

import algebra.Applicative

trait ApplicativeSyntax {
  extension [F[_], A](a: A)
    def pure(using ev: Applicative[F]): F[A] =
      ev.pure(a)

  extension [F[_], A, B](t: (F[A], F[B]))
    def map2[C](fab: (A, B) => C)(using ev: Applicative[F]): F[C] =
      ev.map2(t._1, t._2)(fab)

  extension [F[_], A, B, C](t: (F[A], F[B], F[C]))
    def map3[D](fabc: (A, B, C) => D)(using ev: Applicative[F]): F[D] =
      ev.map3(t._1, t._2, t._3)(fabc)
}
