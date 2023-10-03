package exercises.part3.chapter12

trait Semigroup[A]:
  def combine(x: A, y: A): A

object Semigroup:
  def apply[A](using semigroup: Semigroup[A]): Semigroup[A] = semigroup
