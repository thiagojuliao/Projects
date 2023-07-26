package core.validations

import algebra.Validated
import core.common.Valid
import core.validations.common.*

object account {
  def minimumAvailableLimitValidation(limit: Int): Valid[Int] =
    greaterOrEqualThanValidation(limit, "available limit", 0)
}
