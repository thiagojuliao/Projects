package algebra

import core.Transaction
import Console.{RED, RESET}
import scala.reflect.ClassTag

object instances {
  object show {
    given stringShow: Show[String] = (s: String) => s

    given intShow: Show[Int] = _.toString

    given listShow[A : Show]: Show[List[A]] =
      (ls: List[A]) => ls.map(Show[A].show).mkString("\n")

    given validatedShow[E : Show, A : Show : ClassTag]: Show[Validated[E, A]] = {
      case Invalid(e) =>
        val header =
          s"⚠️ ${RED}A ${implicitly[ClassTag[A]].runtimeClass.getSimpleName} value could not be built because of:"

        val separator = "=" * header.length

        s"""
           |$header
           |$separator
           |${Show[E].show(e)}
           |$separator
           |$RESET""".stripMargin

      case Valid(a) => s"Valid(${Show[A].show(a)})"
    }

    given transactionShow: Show[Transaction] = _.toString
  }

  object semigroup {
    given stringSemigroup: Semigroup[String] = (s1: String, s2: String) => s1 + s2

    given intSemigroup: Semigroup[Int] = (x: Int, y: Int) => x + y

    given listSemigroup[A]: Semigroup[List[A]] = (lsa: List[A], lsb: List[A]) => lsa ::: lsb

    given validatedSemigroup[E : Semigroup, A]: Semigroup[Validated[E, A]] = {
      case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
      case (Invalid(e1), _)           => Invalid(e1)
      case (_, Invalid(e2))           => Invalid(e2)
      case (Valid(a1), _)             => Valid(a1)
    }
  }
}
