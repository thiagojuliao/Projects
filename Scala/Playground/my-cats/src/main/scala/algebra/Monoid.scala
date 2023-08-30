package algebra

trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

object Monoid {
  def apply[A](using ev: Monoid[A]): Monoid[A] = ev
}
