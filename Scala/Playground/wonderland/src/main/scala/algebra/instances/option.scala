package algebra.instances

import algebra.*

object option:
  given [A: Semigroup]: Semigroup[Option[A]] with
    override def combine(o1: Option[A], o2: Option[A]): Option[A] =
      for
        x <- o1
        y <- o2
      yield Semigroup[A].combine(x, y)

  given [A: Monoid]: Monoid[Option[A]] with
    override def empty: Option[A] = Some(Monoid[A].empty)

    override def combine(o1: Option[A], o2: Option[A]): Option[A] =
      for
        x <- o1
        y <- o2
      yield Monoid[A].combine(x, y)

  given Functor[Option] with
    override def map[A, B](o: Option[A])(f: A => B): Option[B] = o.map(f)

  given Applicative[Option] with
    override def pure[A](a: A): Option[A] = Option(a)

    override def ap[A, B](o: Option[A])(of: Option[A => B]): Option[B] =
      for
        a <- o
        f <- of
      yield f(a)

  given Alternative[Option] with
    override def empty[A]: Option[A] = None

    override def orElse[A](o1: Option[A], o2: => Option[A]): Option[A] =
      o1.orElse(o2)

    override def pure[A](a: A): Option[A] = Option(a)

    override def ap[A, B](o: Option[A])(of: Option[A => B]): Option[B] =
      for
        a <- o
        f <- of
      yield f(a)
