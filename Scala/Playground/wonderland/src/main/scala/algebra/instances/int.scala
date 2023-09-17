package algebra.instances

import algebra.*

object int:
  given Semigroup[Int] with
    override def combine(x: Int, y: Int): Int = x + y

  given Monoid[Int] with
    override def empty: Int = 0
    override def combine(x: Int, y: Int): Int = x + y
