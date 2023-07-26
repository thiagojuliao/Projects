import core.*

extension [A](a: A) def show: Unit = println(a)

object Main extends App {
  val account     = Account.create(1500)
  val transaction = Transaction.make("test-merchant", 1500)

  account.show
  transaction.show
}
