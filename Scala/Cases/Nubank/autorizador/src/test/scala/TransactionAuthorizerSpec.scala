import core.*

class TransactionAuthorizerSpec extends TransactionAuthorizerChecks {

  "An authorizer" should {
    "authorize a valid transaction" in {
      val account     = Account.create(100)
      val transaction = Transaction.make("Burguer King", 10)

      checkForValidAuthorization(account, transaction)
    }

    "not authorize an invalid transaction" in {
      val account     = Account.create(100).inactivate
      val transaction = Transaction.make("Paris 6", 100)

      checkForInvalidAuthorization(account, transaction)
    }
  }
}
