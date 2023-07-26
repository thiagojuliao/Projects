package core.validations

import algebra.*
import algebra.syntax.*
import core.common.Valid

object common {
  def neverNullValidation[A](value: A, name: String): Valid[A] =
    Validated.cond(value, List(s"$name should not be null".err))(Option(_).isDefined)

  def greaterOrEqualThanValidation[A](value: A, name: String, lowerBound: A)(using o: Ordering[A]): Valid[A] =
    Validated.cond(value, List(s"$name should be greater or equal than $lowerBound".err))(o.gteq(_, lowerBound))

  def lessOrEqualThanValidation[A](value: A, name: String, upperBound: A)(using o: Ordering[A]): Valid[A] =
    Validated.cond(value, List(s"$name should be less or equal than $upperBound".err))(o.lteq(_, upperBound))
}
