package algebra

trait Show[A] {
  def show(a: A): String
}

object Show {
  def apply[A](using ev: Show[A]): Show[A] = ev
}
