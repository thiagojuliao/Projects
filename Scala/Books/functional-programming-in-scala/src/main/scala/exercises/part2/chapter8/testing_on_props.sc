import exercises.part2.chapter7.Par
import exercises.part2.chapter7.Par.*
import exercises.part2.chapter8.{Gen, Prop}
import exercises.part2.chapter8.Gen.*
import exercises.part1.chapter6.State.*

import java.util.concurrent.{ExecutorService, Executors}
import scala.annotation.targetName

/** 8.2.2 Some simple examples */
val smallInt = Gen.choose(-10, 10)

val maxProp = Prop.forAll(smallInt.nonEmptyList): l =>
  val max = l.max
  l.forall(_ <= max)

maxProp.run()

/** 8.14: Write a property to verify the behavior of List.sorted (see the API docs: http://mng .bz/N4En), which you can use to sort (among other
  * things) a List[Int].9 For instance, List(2, 1, 3).sorted is equal to List(1, 2, 3).
  */
val sortProp = Prop.forAll(smallInt.list): l =>
  val maybeSorted = l.sorted

  maybeSorted
    .foldLeft((true, Int.MinValue)) { case ((b, prev), curr) =>
      (b && prev <= curr, curr)
    }
    ._1

sortProp.run()

/** 8.2.3 Writing a test suite for parallel computations */
val executor: ExecutorService = Executors.newCachedThreadPool

val p2 = Prop.verify:
  val p  = Par.unit(1).map(_ + 1)
  val p2 = Par.unit(2)
  p.run(executor).get == p2.run(executor).get

p2.run()

def equal[A](p: Par[A], p2: Par[A]): Par[Boolean] =
  p.map2(p2)(_ == _)

val p3 = Prop.verify:
  equal(
    Par.unit(1).map(_ + 1),
    Par.unit(2)
  ).run(executor).get

p3.run()

val p4 = Prop.forAll(smallInt): i =>
  equal(
    Par.unit(i).map(_ + 1),
    Par.unit(i + 1)
  ).run(executor).get

p4.run()

val executors: Gen[ExecutorService] = weighted(choose(1, 4).map(Executors.newFixedThreadPool) -> .75, Gen.unit(Executors.newCachedThreadPool) -> .25)

def forAllPar[A](g: Gen[A])(f: A => Par[Boolean]): Prop =
  Prop.forAll(executors.map2(g)((_, _)))((s, a) => f(a).run(s).get)

@targetName("product")
object `**`:
  def unapply[A, B](p: (A, B)) = Some(p)

def forAllPar2[A](g: Gen[A])(f: A => Par[Boolean]): Prop =
  Prop.forAll(executors ** g):
    case s ** a => f(a).run(s).get

def verifyPar(p: Par[Boolean]): Prop =
  forAllPar(Gen.unit(()))(_ => p)

val p5 = verifyPar:
  equal(
    Par.unit(1).map(_ + 1),
    Par.unit(2)
  )

p5.run()

/** 8.17: Express the property about fork from chapter 7—that fork(x) == x. */
// val forkProp = Prop.forAllPar(gpy2)(y => equal(Par.fork(y), y))
