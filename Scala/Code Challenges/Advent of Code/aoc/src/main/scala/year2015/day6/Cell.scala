package year2015.day6

final case class Cell[+A](x: Int, y: Int, value: A):
  def modify[B](f: A => B): Cell[B] =
    copy(value = f(value))
