import exercises.part2.chapter8.Gen
import exercises.part3.chapter10.Monoid
import exercises.part3.chapter10.Monoid.{*, given}
import exercises.part3.chapter10.instances.*
import exercises.part3.chapter10.instances.int.given

/** A String Monoid */
given Monoid[String] with
  override val empty: String = ""

  override def combine(s1: String, s2: String): String = s1 + s2

// Checking if this instance is lawful
val s1 = "Thiago"
val s2 = "Teixeira"
val s3 = "Juliao"

Laws.leftIdentity(s1)
Laws.rightIdentity(s2)
Laws.associative(s1, s2, s3)

/** A List Monoid */
given [A]: Monoid[List[A]] with
  override val empty: List[A] = Nil

  override def combine(x: List[A], y: List[A]): List[A] = x ++ y

// Checking if this instance is lawful
val l1 = List(1, 2, 3)
val l2 = List(4, 5)
val l3 = List(6)

Laws.leftIdentity(l1)
Laws.rightIdentity(l2)
Laws.associative(l1, l2, l3)

/** 10.4: Checking the monoid laws for the recently created monoid instances */
val ints = Gen.choose(-100, 100)

val intAddition       = Monoid[Int]
val intMultiplication = int.intMultiplication

Monoid.Laws.monoidLaws(intAddition, ints).run()
Monoid.Laws.monoidLaws(intMultiplication, ints).run()

val booleanAnd = boolean.booleanAnd
val booleanOr  = boolean.booleanOr

Monoid.Laws.monoidLaws(booleanAnd, Gen.boolean).run()
Monoid.Laws.monoidLaws(booleanOr, Gen.boolean).run()

val optGen       = ints.map(a => if a % 2 == 0 then Some(a) else None)
val optionMonoid = option.optionMonoid[Int](using intAddition)

Monoid.Laws.monoidLaws(optionMonoid, optGen).run()

/** 10.6 */
List(1, 2, 3).foldLeftM[Int](_ + _)(using intAddition)
List(1, 2, 3).foldRightM[Int](_ + _)(using intAddition)

/** 10.7 */
val seq = LazyList.from(1).take(15000).toIndexedSeq
Monoid.foldMapV(seq)(_ * 10)(using intAddition)
Monoid.foldMapV(IndexedSeq(1, 2, 3))(_ * 10)(using intAddition)

/** 10.9: Hard: Use foldMap to detect whether a given IndexedSeq[Int] is ordered. You’ll need to come up with a creative Monoid.
  */
case class Ordered(get: Int, ordered: Boolean)

object Ordered:
  def apply(n: Int): Ordered = Ordered(n, true)

extension (self: Ordered)
  def combine(that: Ordered): Ordered =
    Ordered(self.get max that.get, self.ordered && (self.get <= that.get))

val orderedMonoid: Monoid[Ordered] = new:
  val empty: Ordered = Ordered(Int.MinValue)

  def combine(o1: Ordered, o2: Ordered): Ordered =
    o1.combine(o2)

Monoid.foldMapV(IndexedSeq(1, 2, 3))(Ordered.apply)(using orderedMonoid).ordered
Monoid.foldMapV(IndexedSeq(4, 2, 5, 1, 3))(Ordered.apply)(using orderedMonoid).ordered

/** 10.18: A bag is like a set, except it’s represented by a map that contains one entry per element, with that element as the key and the value under
  * that key as the number of times the element appears in the bag. Use monoids to compute a bag from an IndexedSeq:
  */
def bag[A](as: IndexedSeq[A])(using m: Monoid[Map[A, Int]]): Map[A, Int] =
  as.map(a => Map(a -> 1)).foldLeft(m.empty)(m.combine)

val seq_ = IndexedSeq.range(1, 10).map(_ % 4)
bag(seq_)
