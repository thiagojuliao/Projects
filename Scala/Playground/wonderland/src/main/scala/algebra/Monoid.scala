package algebra

trait Monoid[A] extends Semigroup[A]:
  def empty: A

object Monoid:
  def apply[A](using monoid: Monoid[A]): Monoid[A] = monoid
