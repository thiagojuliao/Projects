package validators

import core.*

final class TransactionAuthorizerValidator(account: Account, transaction: Transaction) extends BusinessRuleValidator {
  private type DefaultInput      = (Account, Transaction)
  override type ValidationResult = List[String]

  private val accountNotActive = makeRule[DefaultInput] { (acc: Account, tx: Transaction) =>
    if !acc.active then "account-not-active" :: Nil else Nil
  }

  private val firstTransactionAboveThreshold = makeRule[DefaultInput] { (acc: Account, tx: Transaction) =>
    val ratio = tx.amount * 1.0 / acc.availableLimit

    if acc.history.isEmpty && ratio > 0.90 then "first-transaction-above-threshold" :: Nil
    else Nil
  }

  override def validateAll: List[String] =
    accountNotActive(account, transaction) ++
      firstTransactionAboveThreshold(account, transaction)
}
