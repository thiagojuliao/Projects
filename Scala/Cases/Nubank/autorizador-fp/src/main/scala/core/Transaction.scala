package core

import algebra.*
import algebra.syntax.*
import core.common.Valid
import core.validations.common.*
import core.validations.transaction.*

import algebra.instances.semigroup.given

import java.time.LocalDateTime

final case class Transaction private (
  merchant: Transaction.Merchant,
  amount: Transaction.Amount,
  time: LocalDateTime
)

object Transaction {
  opaque type Merchant = String
  opaque type Amount   = Int

  private object Merchant {
    def fromString(s: String): Valid[Merchant] =
      neverNullValidation(s, "merchant name")
  }

  private object Amount {
    def fromInt(n: Int): Valid[Amount] =
      minimumAmountValidation(n) combine
        maximumAmountValidation(n)
  }

  def create(merchant: String, amount: Int)(using sg: Semigroup[List[String]]): Valid[Transaction] = {
    lazy val time = LocalDateTime.now()

    (Merchant.fromString(merchant), Amount.fromInt(amount)) match {
      case (Invalid(e1), Invalid(e2)) => sg.combine(e1, e2).invalid[Transaction]
      case (Invalid(e1), _)           => Invalid(e1)
      case (_, Invalid(e2))           => Invalid(e2)
      case (Valid(m), Valid(a))       => Transaction(m, a, time).valid[List[String]]
    }
  }
}
