package exercises.part1.chapter4

import scala.util.Either
import scala.annotation.tailrec

enum Validated[+E, +A]:
  case Valid(get: A)
  case Invalid(errors: E)

  def toEither: Either[E, A] = this match
    case Invalid(e) => Left(e)
    case Valid(a)   => Right(a)

  def map[B](f: A => B): Validated[E, B] = this match
    case Valid(a)        => Valid(f(a))
    case Invalid(errors) => Invalid(errors)

  def map2[EE >: E, B, C](other: Validated[EE, B])(f: (A, B) => C)(combiner: (EE, EE) => EE): Validated[EE, C] =
    this -> other match
      case Valid(a1) -> Valid(a2)       => Valid(f(a1, a2))
      case Valid(_) -> Invalid(ee2)     => Invalid(ee2)
      case Invalid(ee1) -> Valid(_)     => Invalid(ee1)
      case Invalid(ee1) -> Invalid(ee2) => Invalid(combiner(ee1, ee2))

object Validated:
  def fromEither[E, A](e: Either[E, A]): Validated[E, A] = e match
    case Left(es) => Invalid(es)
    case Right(a) => Valid(a)

  def traverse[E, A, B](as: List[A])(f: A => Validated[E, B])(combiner: (E, E) => E): Validated[E, List[B]] =
    @tailrec
    def loop(l: List[A], res: Validated[E, List[B]]): Validated[E, List[B]] =
      l match
        case Nil    => res
        case h :: t => loop(t, res.map2(f(h))(_ :+ _)(combiner))

    loop(as, Valid(Nil))

  def sequence[E, A](as: List[Validated[E, A]])(combiner: (E, E) => E): Validated[E, List[A]] =
    traverse(as)(identity)(combiner)
