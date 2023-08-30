package exercises.part3.chapter10

import exercises.part1.chapter3.Tree
import exercises.part1.chapter3.Tree.*

trait Foldable[F[_]]:
  def foldRight[A, B](fa: F[A])(acc: B)(f: (A, B) => B): B

  def foldLeft[A, B](fa: F[A])(acc: B)(f: (B, A) => B): B

  def foldMap[A, B](fa: F[A])(f: A => B)(using m: Monoid[B]): B

  extension [A](fa: F[A])
    /** 10.15: Any Foldable structure can be turned into a List. Add a toList extension method to the Foldable trait, and provide a concrete
      * implementation in terms of the other methods on Foldable:
      */
    def toList: List[A]

object Foldable:
  def apply[F[_]](using ev: Foldable[F]): Foldable[F] = ev

  /** 10.12: Implement Foldable[List], Foldable[IndexedSeq], and Foldable[LazyList]. Remember that foldRight, foldLeft, and foldMap can all be
    * implemented in terms of each other, but that might not be the most efficient implementation.
    */
  given Foldable[List] with
    override def foldLeft[A, B](as: List[A])(acc: B)(f: (B, A) => B): B =
      as match
        case Nil    => acc
        case h :: t => foldLeft(t)(f(acc, h))(f)

    override def foldRight[A, B](as: List[A])(acc: B)(f: (A, B) => B): B =
      val g = (b: B, a: A) => f(a, b)
      foldLeft(as.reverse)(acc)(g)

    override def foldMap[A, B](as: List[A])(f: A => B)(using m: Monoid[B]): B =
      as.map(f).foldRight(m.empty)(m.combine)

    extension [A](as: List[A]) def toList: List[A] = as

  given Foldable[IndexedSeq] with
    override def foldLeft[A, B](as: IndexedSeq[A])(acc: B)(f: (B, A) => B): B =
      if as.isEmpty then acc
      else foldLeft(as.tail)(f(acc, as(0)))(f)

    override def foldRight[A, B](as: IndexedSeq[A])(acc: B)(f: (A, B) => B): B =
      val g = (b: B, a: A) => f(a, b)
      foldLeft(as.reverse)(acc)(g)

    override def foldMap[A, B](as: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B =
      as.map(f).foldRight(m.empty)(m.combine)

    extension [A](as: IndexedSeq[A]) def toList: List[A] = as.toList

  given Foldable[LazyList] with
    override def foldLeft[A, B](as: LazyList[A])(acc: B)(f: (B, A) => B): B =
      if as.isEmpty then acc
      else foldLeft(as.tail)(f(acc, as.head))(f)

    override def foldRight[A, B](as: LazyList[A])(acc: B)(f: (A, B) => B): B =
      val g = (b: B, a: A) => f(a, b)
      foldLeft(as.reverse)(acc)(g)

    override def foldMap[A, B](as: LazyList[A])(f: A => B)(using m: Monoid[B]): B =
      as.map(f).foldRight(m.empty)(m.combine)

    extension [A](as: LazyList[A]) def toList: List[A] = as.toList

  /** 10.13: Recall the binary Tree data type from chapter 3. Implement a Foldable instance for it. */
  given Foldable[Tree] with
    override def foldLeft[A, B](fa: Tree[A])(acc: B)(f: (B, A) => B): B =
      fa match
        case Leaf(value)         => f(acc, value)
        case Branch(left, right) => foldLeft(left)(foldLeft(right)(acc)(f))(f)

    override def foldRight[A, B](fa: Tree[A])(acc: B)(f: (A, B) => B): B =
      fa match
        case Leaf(value)         => f(value, acc)
        case Branch(left, right) => foldRight(left)(foldRight(right)(acc)(f))(f)

    override def foldMap[A, B](fa: Tree[A])(f: A => B)(using m: Monoid[B]): B =
      fa match
        case Leaf(value)         => f(value)
        case Branch(left, right) => m.combine(foldMap(left)(f), foldMap(right)(f))

    extension [A](ta: Tree[A])
      def toList: List[A] = ta match
        case Leaf(value)         => List(value)
        case Branch(left, right) => left.toList ++ right.toList

  /** 10.14: Write a Foldable[Option] instance. */
  given Foldable[Option] with
    override def foldLeft[A, B](fa: Option[A])(acc: B)(f: (B, A) => B): B =
      fa match
        case None        => acc
        case Some(value) => f(acc, value)

    override def foldRight[A, B](fa: Option[A])(acc: B)(f: (A, B) => B): B =
      val g = (b: B, a: A) => f(a, b)
      foldLeft(fa)(acc)(g)

    override def foldMap[A, B](fa: Option[A])(f: A => B)(using m: Monoid[B]): B =
      foldLeft(fa.map(f))(m.empty)(m.combine)

    extension [A](oa: Option[A])
      def toList: List[A] = oa match
        case None    => Nil
        case Some(a) => List(a)
