package algebra

sealed trait Validated[+E, +A] {
  def combine[E1 >: E, A1 >: A](that: Validated[E1, A1])(using ev: Semigroup[Validated[E1, A1]]): Validated[E1, A1] =
    ev.combine(this, that)

  def map[B](f: A => B): Validated[E, B] =
    this match {
      case Invalid(e) => Invalid(e)
      case Valid(a)   => Valid(f(a))
    }
}

final case class Invalid[+E, +A](e: E) extends Validated[E, A]
final case class Valid[+E, +A](a: A)   extends Validated[E, A]

object Validated {
  def valid[E, A](a: A): Validated[E, A] = Valid(a)

  def invalid[E, A](e: E): Validated[E, A] = Invalid(e)

  def cond[E, A](a: A, e: E)(predicate: A => Boolean): Validated[E, A] =
    if (predicate(a)) Valid(a) else Invalid(e)
}
