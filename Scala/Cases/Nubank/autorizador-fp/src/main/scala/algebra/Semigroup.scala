package algebra

trait Semigroup[A] {
  def combine(x: A, y: A): A
}

object Semigroup {
  def apply[A](using ev: Semigroup[A]): Semigroup[A] = ev
}
