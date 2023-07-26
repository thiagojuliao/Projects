package core.validations

import algebra.Validated
import algebra.syntax.*
import core.common.Valid
import core.validations.common.*

object transaction {
  def minimumAmountValidation(amount: Int): Valid[Int] =
    greaterOrEqualThanValidation(amount, "transaction amount", 1)

  def maximumAmountValidation(amount: Int): Valid[Int] =
    lessOrEqualThanValidation(amount, "transaction amount", 1500)
}
