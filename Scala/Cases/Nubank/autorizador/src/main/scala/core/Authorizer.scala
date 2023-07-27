package core

import validators.TransactionAuthorizerValidator

sealed trait Authorizer {
  type AuthorizationResult

  def authorize: AuthorizationResult
}

final class TransactionAuthorizer(account: Account, transaction: Transaction) extends Authorizer {
  private val validator = new TransactionAuthorizerValidator(account, transaction)

  private type Violations           = validator.ValidationResult
  override type AuthorizationResult = (Account, Violations)

  private def applyTransaction: Account =
    account
      .updateLimit(_ - transaction.amount)
      .addTransaction(transaction)

  override def authorize: (Account, Violations) =
    val violations = validator.validateAll

    if violations.isEmpty then (applyTransaction, violations)
    else (account, violations)
}
