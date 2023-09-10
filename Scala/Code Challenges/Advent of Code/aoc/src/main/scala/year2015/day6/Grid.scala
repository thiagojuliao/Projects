package year2015.day6

type Grid[+A] = IndexedSeq[Cell[A]]

object Grid:
  def make[A](n: Int, m: Int)(init: => A): Grid[A] =
    for
      x <- 0 until n
      y <- 0 until m
    yield Cell(x, y, init)

  def fromList[A](ss: List[String])(f: Char => A): Grid[A] =
    ss.zipWithIndex.flatMap((input, x) => input.zipWithIndex.map((char, y) => Cell(x, y, f(char)))).toIndexedSeq

  extension [A](self: Grid[A])
    def corners: List[(Int, Int)] =
      val totalCorners = self.maxBy(_.x).x
      List((0, 0), (0, totalCorners), (totalCorners, 0), (totalCorners, totalCorners))

    def pprint(printer: A => String): Unit =
      self
        .groupBy(_.x)
        .toList
        .sortBy(_._1)
        .foreach((x, cells) => println(s"$x: ${cells.map(cell => printer(cell.value)).toList.mkString("")}"))

    def neighbors(cell: Cell[A]): List[Cell[A]] =
      self.filter { case nc @ Cell(x, y, _) =>
        val deltaX = (cell.x - x).abs
        val deltaY = (cell.y - y).abs

        (deltaX == 0 || deltaX == 1) && (deltaY == 0 || deltaY == 1) && (nc != cell)
      }.toList

    def modify(f: Cell[A] => Cell[A]): Grid[A] = self.map(f)

    def modify(f: (Grid[A], Cell[A]) => Cell[A]): Grid[A] = self.map(f(self, _))

    def modifyN(n: Int)(f: (Grid[A], Cell[A]) => Cell[A]): Grid[A] =
      Iterator.iterate(self)(_.modify(f)).take(n).toList.last

    def debugWith(iterations: Int)(f: (Grid[A], Cell[A]) => Cell[A], printer: A => String): Unit =
      Iterator
        .iterate(self)(_.modify(f))
        .take(iterations)
        .zipWithIndex
        .foreach { (grid, iteration) =>
          println(s"##### Iteration $iteration #####")
          grid.pprint(printer)
          println()
        }
