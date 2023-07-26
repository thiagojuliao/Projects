package core

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Transaction private (
  merchant: String,
  amount: Int,
  time: LocalDateTime
) {
  override def toString: String =
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")

    s"Transaction(merchant=$merchant, amount=$amount, time=${time.format(formatter)})"
}

object Transaction {
  def make(merchant: String, amount: Int): Transaction =
    Transaction(
      merchant = merchant,
      amount = amount,
      time = LocalDateTime.now()
    )
}
