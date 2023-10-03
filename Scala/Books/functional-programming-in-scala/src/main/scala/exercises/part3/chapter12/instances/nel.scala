package exercises.part3.chapter12.instances

import exercises.part3.chapter12.Semigroup
import exercises.part3.chapter12.NonEmptyList

object nel:
  given [A]: Semigroup[NonEmptyList[A]] with
    override def combine(x: NonEmptyList[A], y: NonEmptyList[A]): NonEmptyList[A] =
      NonEmptyList(x.head, x.tail ++ (y.head :: y.tail))
