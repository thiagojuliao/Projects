import org.scalatest.wordspec.AnyWordSpec

import core.*

class TransactionAuthorizerChecks extends AnyWordSpec {

  def checkForValidAuthorization(account: Account, transaction: Transaction): Unit =
    val authorizer                       = TransactionAuthorizer
    val (maybeUpdateAccount, violations) = authorizer.authorize(account, transaction)

    assert(violations.isEmpty)
    assert(account.availableLimit - maybeUpdateAccount.availableLimit == transaction.amount)
    assert(maybeUpdateAccount.history.contains(transaction))

  def checkForInvalidAuthorization(account: Account, transaction: Transaction): Unit =
    val authorizer                       = TransactionAuthorizer
    val (maybeUpdateAccount, violations) = authorizer.authorize(account, transaction)

    assert(violations.nonEmpty)
    assert(account.availableLimit == maybeUpdateAccount.availableLimit)
    assert(account.history == maybeUpdateAccount.history)
}
