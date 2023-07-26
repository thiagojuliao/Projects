import core.*
import org.scalatest.wordspec.AnyWordSpec

trait AccountChecks extends AnyWordSpec {
  protected def checkForInactivation(account: Account): Unit =
    assert(!account.inactivate.active)

  protected def checkForNewLimitAfterSetLimit(account: Account, newLimit: Int): Unit =
    assert(
      account.setLimit(newLimit).availableLimit == newLimit
    )

  protected def checkForNewLimitAfterUpdateLimit(account: Account, f: Int => Int): Unit =
    val oldLimit = account.availableLimit

    assert(
      account.updateLimit(f).availableLimit == f(oldLimit)
    )

  protected def checkForNewTransaction(account: Account, transaction: Transaction): Unit =
    assert(
      account.addTransaction(transaction).history.contains(transaction)
    )
}
