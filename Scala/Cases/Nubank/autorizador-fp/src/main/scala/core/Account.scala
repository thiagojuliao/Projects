package core

import core.common.*
import algebra.syntax.*
import core.validations.account.*

final case class Account private (
  active: Boolean,
  availableLimit: Account.AvailableLimit,
  history: List[Valid[Transaction]]
) {
  def inactivate: Account =
    copy(active = false)
}

object Account {
  opaque type AvailableLimit = Int

  private object AvailableLimit {
    def fromInt(n: Int): Valid[AvailableLimit] =
      minimumAvailableLimitValidation(n)
  }

  def create(initialLimit: Int): Valid[Account] =
    AvailableLimit.fromInt(initialLimit).map { validLimit =>
      Account(
        active = true,
        availableLimit = validLimit,
        history = List.empty
      )
    }
}
