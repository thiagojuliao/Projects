package year2015

export extensions.*

object extensions:
  extension [A](a: A) def show: Unit = println(a.toString)

  extension (n: Int)
    def between(x: Int, y: Int): Boolean =
      x <= n && n <= y
