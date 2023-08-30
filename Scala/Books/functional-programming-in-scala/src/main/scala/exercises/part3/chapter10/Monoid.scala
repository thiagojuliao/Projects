package exercises.part3.chapter10

import exercises.part2.chapter8.{Gen, Prop}

trait Monoid[A]:
  def empty: A
  def combine(x: A, y: A): A

object Monoid:
  def apply[A](using m: Monoid[A]): Monoid[A] = m

  /** 10.7: Implement a foldMap for IndexedSeq. Your implementation should use the strategy of splitting the sequence in two, recursively processing
    * each half, and then adding the answers together with the monoid:
    */
  def foldMapV[A, B](as: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B =
    def rec(seq: IndexedSeq[A]): B =
      if seq.isEmpty then m.empty
      else if seq.length == 1 then f(seq(0))
      else
        val (a1, a2) = seq.splitAt(seq.length / 2)
        m.combine(rec(a1), rec(a2))
    rec(as)

  /** 10.8: Hard: Implement a parallel version of foldMap using the library we developed in chapter 7. Hint: Implement par, a combinator to promote
    * Monoid[A] to a Monoid [Par[A]],5 and then use this to implement parFoldMap:
    */
  object Laws:
    def leftIdentity[A](a: A)(using m: Monoid[A]): Boolean =
      m.combine(m.empty, a) == a

    def rightIdentity[A](a: A)(using m: Monoid[A]): Boolean =
      m.combine(a, m.empty) == a

    def associative[A](x: A, y: A, z: A)(using m: Monoid[A]): Boolean =
      m.combine(m.combine(x, y), z) == m.combine(x, m.combine(y, z))

    /** 10.4: Use the property-based testing framework we developed in part 2 to implement a property for the monoid laws. Use your property to test
      * the monoids we’ve written
      */
    def monoidLaws[A](m: Monoid[A], ge: Gen[A]): Prop =
      Prop.forAll(ge)(leftIdentity(_)(using m)) &&
        Prop.forAll(ge)(rightIdentity(_)(using m)) &&
        Prop.forAll(ge.listOfN(3).map(_.toVector))(l => associative(l(0), l(1), l(2))(using m))

  extension [A](ls: List[A])
    def combineAll(using m: Monoid[A]): A =
      ls.foldLeft(m.empty)(m.combine)

    /** 10.5: Implement foldMap */
    def foldMap[B](f: A => B)(using m: Monoid[B]): B =
      ls.map(f).foldLeft(m.empty)(m.combine)

    /** 10.6: Hard: The foldMap function can be implemented using either foldLeft or foldRight, but you can also write foldLeft and foldRight using
      * foldMap. Try it!
      */
    def foldLeftM[B](f: (B, A) => B)(using m: Monoid[B]): B =
      val g = (a: A) => f.curried(m.empty)(a)
      ls.foldMap(g)(using m)

    def foldRightM[B](f: (A, B) => B)(using m: Monoid[B]): B =
      val g = (a: A) => f(a, m.empty)
      ls.foldMap(g)(using m)

  /** 10.16: Implement productMonoid using a ma: Monoid[A] and mb: Monoid[B]. Notice that your implementation of combine is associative so long as
    * ma.combine and mb.combine are both associative:
    */
  given productMonoid[A, B](using ma: Monoid[A], mb: Monoid[B]): Monoid[(A, B)] with
    def combine(x: (A, B), y: (A, B)): (A, B) =
      val (a1, b1) = x; val (a2, b2) = y
      ma.combine(a1, a2) -> mb.combine(b1, b2)

    val empty: (A, B) = ma.empty -> mb.empty

  /** Listing 10.1 Merging key-value Maps */
  given mapMergeMonoid[K, V](using mv: Monoid[V]): Monoid[Map[K, V]] with
    def combine(a: Map[K, V], b: Map[K, V]): Map[K, V] =
      (a.keySet ++ b.keySet).foldLeft(empty): (acc, k) =>
        acc.updated(k, mv.combine(a.getOrElse(k, mv.empty), b.getOrElse(k, mv.empty)))

    val empty: Map[K, V] = Map()

  /** 10.17: Write a monoid instance for functions whose results are monoids: */
  given functionMonoid[A, B](using mb: Monoid[B]): Monoid[A => B] with
    def combine(f: A => B, g: A => B): A => B =
      a => mb.combine(f(a), g(a))

    val empty: A => B = _ => mb.empty
