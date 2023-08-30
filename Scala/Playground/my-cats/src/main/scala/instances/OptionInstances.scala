package instances

import algebra.Applicative

trait OptionInstances {
  given optionApplicative: Applicative[Option] = new Applicative[Option]:
    override def pure[A](a: A): Option[A] = Option(a)

    override def app[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
      fab match {
        case None    => None
        case Some(f) => fa.map(f)
      }
}
