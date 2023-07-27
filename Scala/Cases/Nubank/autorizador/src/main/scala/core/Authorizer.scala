package core

import validators.TransactionAuthorizerValidator

sealed trait Authorizer {
  type Env
  type AuthorizationResult

  val authorize: Env => AuthorizationResult
}

object TransactionAuthorizer extends Authorizer {
  private val validator = TransactionAuthorizerValidator

  private type Violations           = validator.ValidationResult
  override type Env                 = (Account, Transaction)
  override type AuthorizationResult = (Account, Violations)

  private def applyTransaction(account: Account, transaction: Transaction): Account =
    account
      .updateLimit(_ - transaction.amount)
      .addTransaction(transaction)

  override val authorize: ((Account, Transaction)) => (Account, Violations) = { (acc: Account, tx: Transaction) =>
    val violations = validator.validateAll(acc, tx)

    if violations.isEmpty then (applyTransaction(acc, tx), Nil)
    else (acc, violations)
  }
}
