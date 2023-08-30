package year2015.day6

type Grid[+A] = IndexedSeq[Cell[A]]

object Grid:
  def make[A](n: Int, m: Int)(init: => A): Grid[A] =
    for
      x <- 0 until n
      y <- 0 until m
    yield Cell(x, y, init)
