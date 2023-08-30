import exercises.part1.chapter6.SimpleRNG
import exercises.part2.chapter7.Par
import exercises.part2.chapter7.Par.*
import exercises.part2.chapter8.Gen
import exercises.part2.chapter8.Gen.*

val rng = SimpleRNG(26)

/** 8.4 */
choose(5, 15).run(rng)

/** 8.5 */
unit(56).run(rng)
boolean.run(rng)
choose(-10, 10).listOfN(10).run(rng)
boolean.listOfN(10).run(rng)

/** 8.16: Hard: Write a richer generator for Par[Int] that builds more deeply nested parallel computations than the simple ones we gave previously. */
val gpy2: Gen[Par[Int]] =
  choose(-100, 100).listOfN(choose(0, 20)).map(ys => ys.foldLeft(Par.unit(0))((p, y) => Par.fork(p.map2(Par.unit(y))(_ + _))))

extension [A](self: List[A])
  def parTraverse[B](f: A => Par[B]): Par[List[B]] =
    self.foldRight(Par.unit(Nil: List[B]))((a, pacc) => Par.fork(f(a).map2(pacc)(_ :: _)))

val gpy3: Gen[Par[Int]] =
  choose(-100, 100).listOfN(choose(0, 20)).map(ys => ys.parTraverse(Par.unit).map(_.sum))
