package core

import validators.TransactionAuthorizerValidator

sealed trait Authorizer[Env, R] {
  def authorize: Env => R
}

object TransactionAuthorizer extends Authorizer[(Account, Transaction), (Account, List[String])] {
  private val validator = TransactionAuthorizerValidator

  private type Violations = List[String]

  private def applyTransaction(account: Account, transaction: Transaction): Account =
    account
      .updateLimit(_ - transaction.amount)
      .addTransaction(transaction)

  override def authorize: ((Account, Transaction)) => (Account, Violations) = { (acc: Account, tx: Transaction) =>
    val violations = validator.validateAll(acc, tx)

    if violations.isEmpty then (applyTransaction(acc, tx), Nil)
    else (acc, violations)
  }
}
