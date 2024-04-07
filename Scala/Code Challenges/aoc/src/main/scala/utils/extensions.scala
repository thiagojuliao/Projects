package utils

export extensions.*

object extensions:
  extension [A](a: A)
    def show: Unit = println(a.toString)

    def let[B](f: A => B): B = f(a)

  extension (n: Int)
    def between(x: Int, y: Int): Boolean =
      x <= n && n <= y
