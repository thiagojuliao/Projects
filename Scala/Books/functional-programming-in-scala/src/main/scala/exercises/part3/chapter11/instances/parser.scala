package exercises.part3.chapter11.instances

import exercises.part2.chapter9.Parser
import exercises.part2.chapter9.Parser.*
import exercises.part3.chapter11.Monad

object parser:
  /** 11.1: Write monad instances for Option, List, LazyList, Par, and Parser. */
  given Monad[Parser] with
    override def unit[A](a: => A): Parser[A] = Parser.succeed(a)

    override def flatMap[A, B](fa: Parser[A])(f: A => Parser[B]): Parser[B] =
      fa.flatMap(f)
