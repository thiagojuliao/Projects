import core.*
import org.scalatest.Assertion
import org.scalatest.wordspec.AnyWordSpec

class AccountSpec extends AccountChecks with SharedTestEnv {
  "An account" should {
    "be inactive after an inactivation" in {
      checkForInactivation(mockAccount)
    }

    "have a new limit after an update operation" in {
      // Testing the setLimit method
      checkForNewLimitAfterSetLimit(mockAccount, 5000)

      // Testing the updateLimit method
      checkForNewLimitAfterUpdateLimit(mockAccount, _ - 300)
    }

    "contain a new transaction in its history after making a new one" in {
      checkForNewTransaction(mockAccount, mockTransaction)
    }
  }
}
