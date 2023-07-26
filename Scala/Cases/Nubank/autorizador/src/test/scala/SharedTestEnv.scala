import core.*

trait SharedTestEnv {
  protected val mockAccount: Account = Account.create(1500)

  protected val mockTransaction: Transaction = Transaction.make("test-merchant", 750)
}
