package exercises.part1.chapter4

import scala.annotation.tailrec
import scala.util.control.NonFatal

enum Either[+E, +A]:
  case Left(e: E)
  case Right(a: A)

  /** 4.6: Implement versions of map, flatMap, orElse, and map2 on Either that operate on the Right value
    */
  def map[B](f: A => B): Either[E, B] = this match
    case Left(e)  => Left(e)
    case Right(a) => Right(f(a))

  def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] = this match
    case Left(e)  => Left(e)
    case Right(a) => f(a)

  def orElse[EE >: E, B >: A](b: => Either[EE, B]): Either[EE, B] = this match
    case Left(_) => b
    case _       => this

  def map2[EE >: E, B, C](other: Either[EE, B])(f: (A, B) => C): Either[EE, C] =
    this -> other match
      case Left(e1) -> _          => Left(e1)
      case _ -> Left(e2)          => Left(e2)
      case Right(a1) -> Right(a2) => Right(f(a1, a2))

  /** Auxiliary methods */
  def mapError[EE](f: E => EE): Either[EE, A] = this match
    case Left(e)  => Left(f(e))
    case Right(a) => Right(a)

object Either:
  def catchNonFatal[A](a: => A): Either[Throwable, A] =
    try Right(a)
    catch case NonFatal(t) => Left(t)

  /** 4.7: Implement sequence and traverse for Either. These should return the first error that’s encountered if there is one
    */
  def traverse[E, A, B](as: List[A])(f: A => Either[E, B]): Either[E, List[B]] =
    @tailrec
    def loop(l: List[A], res: Either[E, List[B]]): Either[E, List[B]] =
      l match
        case Nil    => res
        case h :: t =>
          f(h) match
            case Left(e)  => Left(e)
            case Right(a) => loop(t, res.map(_ :+ a))

    loop(as, Right(Nil))

  def sequence[E, A](as: List[Either[E, A]]): Either[E, List[A]] =
    traverse(as)(identity)

  def map2Both[E, A, B, C](
      a: Either[E, A],
      b: Either[E, B],
      f: (A, B) => C
  ): Either[List[E], C] =
    (a, b) match
      case (Right(aa), Right(bb)) => Right(f(aa, bb))
      case (Left(e), Right(_))    => Left(List(e))
      case (Right(_), Left(e))    => Left(List(e))
      case (Left(e1), Left(e2))   => Left(List(e1, e2))

  def map2All[E, A, B, C](
      a: Either[List[E], A],
      b: Either[List[E], B],
      f: (A, B) => C
  ): Either[List[E], C] =
    (a, b) match
      case (Right(aa), Right(bb)) => Right(f(aa, bb))
      case (Left(es), Right(_))   => Left(es)
      case (Right(_), Left(es))   => Left(es)
      case (Left(es1), Left(es2)) => Left(es1 ++ es2)

  def traverseAll[E, A, B](
      as: List[A],
      f: A => Either[List[E], B]
  ): Either[List[E], List[B]] =
    as.foldRight(Right(Nil): Either[List[E], List[B]])((a, acc) => map2All(f(a), acc, _ :: _))

  def sequenceAll[E, A](
      as: List[Either[List[E], A]]
  ): Either[List[E], List[A]] =
    traverseAll(as, identity)
