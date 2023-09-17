package algebra

trait Semigroup[A]:
  def combine(x: A, y: A): A

object Semigroup:
  def apply[A](using semigroup: Semigroup[A]): Semigroup[A] = semigroup

  object syntax:
    extension [A: Semigroup](x: A) def |+|(y: A): A = Semigroup[A].combine(x, y)
