package algebra.instances

import algebra.*

object string:
  given Semigroup[String] with
    override def combine(x: String, y: String): String = x + y

  given Monoid[String] with
    override def empty: String = ""

    override def combine(x: String, y: String): String = x + y
