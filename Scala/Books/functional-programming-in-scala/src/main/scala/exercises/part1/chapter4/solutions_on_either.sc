import exercises.part1.chapter4.Either
import exercises.part1.chapter4.Either.*

/** 4.6: Testing */
type ErrorOr[T] = Either[String, T]

def errorOr[A](msg: String): ErrorOr[A] = Left(msg)

// map
Right(42).map(_ + 1)
errorOr[Int]("simple error").map(_ * 2)

// flatMap
Right(34).flatMap(a => if a % 2 == 0 then Right(a) else Left("odd number"))
Right(31).flatMap(a => if a % 2 == 0 then Right(a) else Left("odd number"))
errorOr[Int]("odd number").flatMap(Right(_))

// orElse
Right("Thiago").orElse(Right("Juliao"))
Left("wrong name").orElse(Right("Julius"))

// map2
case class Account private (name: String, age: Int)

object Account:
  private def parseName(name: String): ErrorOr[String] =
    if name.isEmpty || name.length < 5 then Left("invalid name") else Right(name)

  private def parseAge(age: Int): ErrorOr[Int] =
    if age <= 18 then Left("invalid age") else Right(age)

  def make(name: String, age: Int): ErrorOr[Account] =
    parseName(name).map2(parseAge(age))(Account.apply)

Account.make("", 15)
Account.make("Thiago", 15)
Account.make("Thiago", 34)

/** 4.7: Testing */
def parseInt(s: String): ErrorOr[Int] =
  catchNonFatal(s.toInt).mapError(_.toString)

traverse(List("1", "2"))(parseInt)
traverse(List("1", "a"))(parseInt)

sequence(List(Right(42), Right(15)))
sequence(List(Right(42), Left("fail!")))
