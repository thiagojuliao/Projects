import algebra.*
import algebra.instances.show.given
import algebra.instances.semigroup.given
import algebra.syntax.*
import core.Transaction

object Main extends App {
  val invalid: Validated[List[String], Int] = Invalid(
    List("Invalid name", "Invalid age", "Invalid address")
      .map(_.err)
  )

  println(invalid.show)

  val invalidFromCond = Validated.cond(42, List("Number must be greater than 100!".err))(_ > 100)

  println(invalidFromCond.show)

  val combinedInvalid = invalid.combine(invalidFromCond)

  println(combinedInvalid.show)

  val validTransaction = Transaction.create("Lolly Moons", 550)
  println(validTransaction.show)

  val invalidTransaction = Transaction.create(null, -1)
  println(invalidTransaction.show)
}
