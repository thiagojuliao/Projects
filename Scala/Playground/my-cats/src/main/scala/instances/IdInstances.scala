package instances

import algebra.*
import data.Kleisli.Id

trait IdInstances {
  given idMonad: Monad[Id] = new Monad[Id]:
    override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

    override def pure[A](a: A): Id[A] = a

  given idApplicative: Applicative[Id] = new Applicative[Id]:
    override def pure[A](a: A): Id[A] = a

    override def app[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = fab(fa)
}
