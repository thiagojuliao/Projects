package algebra

trait Eq[A] {
  def eqv(x: A, y: A): Boolean
}

object Eq {
  def apply[A](using ev: Eq[A]): Eq[A] = ev
}
