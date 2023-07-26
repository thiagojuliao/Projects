package core

case class Account private (
  active: Boolean,
  availableLimit: Int,
  history: List[Transaction]
) {
  def inactivate: Account =
    copy(active = false)

  def setLimit(newLimit: Int): Account =
    updateLimit(_ => newLimit)

  def updateLimit(f: Int => Int): Account =
    copy(availableLimit = f(this.availableLimit))

  def addTransaction(ts: Transaction): Account =
    copy(history = ts :: this.history)

  override def toString: String =
    s"Account(active=$active, availableLimit=$availableLimit, history=$history)"
}

object Account {
  def create(initialLimit: Int): Account =
    Account(
      active = true,
      availableLimit = initialLimit,
      history = List.empty
    )
}
