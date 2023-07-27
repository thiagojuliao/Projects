package validators

import core.*

object TransactionAuthorizerValidator extends BusinessRuleValidator {
  private type DefaultInput      = (Account, Transaction)
  override type Env              = DefaultInput
  override type ValidationResult = List[String]

  private val accountNotActive = makeRule[DefaultInput] { (acc: Account, tx: Transaction) =>
    if !acc.active then "account-not-active" :: Nil else Nil
  }

  private val firstTransactionAboveThreshold = makeRule[DefaultInput] { (acc: Account, tx: Transaction) =>
    val ratio = tx.amount * 1.0 / acc.availableLimit

    if acc.history.isEmpty && ratio > 0.90 then "first-transaction-above-threshold" :: Nil
    else Nil
  }

  override val validateAll: ((Account, Transaction)) => List[String] = { (acc: Account, tx: Transaction) =>
    accountNotActive(acc, tx) ++ firstTransactionAboveThreshold(acc, tx)
  }
}
