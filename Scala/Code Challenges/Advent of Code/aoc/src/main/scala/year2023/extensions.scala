package year2023

object extensions:
  extension [A](self: A) def print: Unit = println(self.toString)

  extension [K](self: Map[K, Int])
    def +(other: Map[K, Int]): Map[K, Int] =
      self.foldLeft(other) { case (acc, (k, v)) =>
        val v_ = acc.getOrElse(k, 0) + v
        acc + (k -> v_)
      }
