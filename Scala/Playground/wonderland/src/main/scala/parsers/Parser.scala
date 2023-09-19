package parsers

case class Parser[A](run: String => Option[(String, A)])

object Parser:
  def sequence[A](pas: List[Parser[A]]): Parser[List[A]] = Parser { s1 =>
    pas.foldLeft(Option((s1, Nil: List[A]))) {
      case None -> _            => None
      case Some(s2 -> as) -> pa => pa.run(s2).map((s, a) => (s, as :+ a))
    }
  }

  def many[A](pa: Parser[A]): Parser[List[A]] =
    Parser { s1 =>
      Iterator
        .iterate(Option((s1, Nil: List[A]))) {
          case None            => None
          case Some(s2 -> acc) => pa.run(s2).map((s3, a) => (s3, acc :+ a))
        }
        .takeWhile(_.isDefined)
        .toList
        .last
    }
