package algebra.instances

import algebra.*
import algebra.Alternative.syntax.<|>
import algebra.instances.option.given
import parsers.Parser

object parser:
  given Functor[Parser] with
    override def map[A, B](pa: Parser[A])(f: A => B): Parser[B] = Parser { s1 =>
      pa.run(s1).map((s2, a) => (s2, f(a)))
    }

  given Applicative[Parser] with
    override def pure[A](a: A): Parser[A] = Parser(s => Some((s, a)))

    override def ap[A, B](pa: Parser[A])(pf: Parser[A => B]): Parser[B] =
      Parser { s1 =>
        for
          case (s2, f) <- pf.run(s1)
          case (s3, a) <- pa.run(s2)
        yield (s3, f(a))
      }

  given Alternative[Parser] with
    override def empty[A]: Parser[A] = Parser(_ => None)

    override def orElse[A](p1: Parser[A], p2: => Parser[A]): Parser[A] =
      Parser(s => p1.run(s) <|> p2.run(s))

    override def pure[A](a: A): Parser[A] = Parser(s => Some(s -> a))

    override def ap[A, B](pa: Parser[A])(pf: Parser[A => B]): Parser[B] =
      Parser { s1 =>
        for
          case (s2, f) <- pf.run(s1)
          case (s3, a) <- pa.run(s2)
        yield (s3, f(a))
      }
