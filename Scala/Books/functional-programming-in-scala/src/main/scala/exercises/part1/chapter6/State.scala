package exercises.part1.chapter6

import scala.annotation.tailrec

/** 6.10: Generalize the functions unit, map, map2, flatMap, and sequence. Add them as extension methods on the State type where possible. Otherwise,
  * you should put them in the State companion object.
  */
case class State[S, +A](run: S => (A, S)):
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State { s =>
      val (a, s1) = run(s)
      f(a).run(s1)
    }

  def map[B](f: A => B): State[S, B] =
    flatMap(a => State.unit(f(a)))

  def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
    flatMap(a => sb.map(b => f(a, b)))

object State:
  def unit[S, A](a: A): State[S, A] = State(s => (a, s))

  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] =
    as.foldLeft(unit[S, List[B]](Nil))((acc, a) => f(a).map2(acc)(_ :: _))

  def sequence[S, A](sas: List[State[S, A]]): State[S, List[A]] =
    sas.foldLeft(unit(Nil: List[A]))((acc, s) => s.map2(acc)(_ :: _))

  def get[S]: State[S, S] = State(s => (s, s))

  def set[S](s: S): State[S, Unit] = State(_ => ((), s))

  def modify[S](f: S => S): State[S, Unit] =
    for
      s <- get
      _ <- set(f(s))
    yield ()
