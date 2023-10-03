package exercises.part3.chapter12.instances

import exercises.part1.chapter4.Validated
import exercises.part1.chapter4.Validated.*
import exercises.part3.chapter12.{Applicative, Semigroup}

object validated:
  /** 12.6: Write an Applicative instance for Validated that accumulates errors in Invalid. In chapter 4, we modified the signature of map2 to take an
    * extra combineErrors: (E, E) \=> E function. In this exercise, use the monoid for the error type to combine errors. Remember that the summon
    * method can be used to get an explicit reference to a context parameter
    */
  given [E: Semigroup]: Applicative[[A] =>> Validated[E, A]] with
    override def unit[A](a: => A): Validated[E, A] = Valid(a)

    override def ap[A, B](fa: Validated[E, A])(fab: Validated[E, A => B]): Validated[E, B] =
      fa -> fab match
        case Invalid(e1) -> Invalid(e2) => Invalid(Semigroup[E].combine(e1, e2))
        case Invalid(e1) -> _           => Invalid(e1)
        case _ -> Invalid(e2)           => Invalid(e2)
        case Valid(a) -> Valid(f)       => Valid(f(a))
