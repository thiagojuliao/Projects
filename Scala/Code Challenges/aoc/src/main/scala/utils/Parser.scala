package utils

final case class Parser[A](run: String => (String, A)):
  def map[B](f: A => B): Parser[B] = Parser { s =>
    val (s1, a) = this.run(s)
    s1 -> f(a)
  }

  def flatMap[B](f: A => Parser[B]): Parser[B] = Parser { s =>
    val (s1, a) = this.run(s); val (s2, b) = f(a).run(s1)
    s2 -> b
  }

object Parser:
  def pure[A](a: A): Parser[A] = Parser(s => (s, a))
