package exercises.part1.chapter5

import scala.annotation.tailrec

enum LazyList[+A]:
  case Empty
  case Cons(h: () => A, t: () => LazyList[A])

  def headOption: Option[A] = this match
    case Empty      => None
    case Cons(h, _) => Some(h())

  /** Obs.: This definition of exists, though illustrative, isn’t stack safe if the lazy list is large and all elements test false. */
  def exists(p: A => Boolean): Boolean =
    foldRight(false)((a, b) => p(a) || b)

  def foldRight[B](acc: => B)(f: (A, => B) => B): B =
    this match
      case Cons(h, t) => f(h(), t().foldRight(acc)(f))
      case _          => acc

  def find(p: A => Boolean): Option[A] =
    filter(p).headOption

  override def toString: String = "LazyList#NotComputed"

  /** 5.1: Write a function to convert a LazyList to a List, which will force its evaluation and let you look at it in the REPL
    */
  def toList: List[A] =
    @tailrec
    def build(ll: LazyList[A], res: List[A]): List[A] =
      ll match
        case Empty      => res.reverse
        case Cons(h, t) => build(t(), h() :: res)

    build(this, Nil)

  /** 5.2: Write the function take(n) for returning the first n elements of a LazyList and drop(n) for skipping the first n elements of a LazyList.
    */
  def take(n: Int): LazyList[A] =
    @tailrec
    def loop(ll: LazyList[A], count: Int, res: List[A]): LazyList[A] =
      ll match
        case Cons(h, t) if count > 0 => loop(t(), count - 1, h() :: res)
        case _                       => LazyList(res.reverse*)

    loop(this, n, Nil)

  def drop(n: Int): LazyList[A] =
    @tailrec
    def loop(rest: LazyList[A], count: Int): LazyList[A] =
      rest match
        case Cons(_, t) if count > 0 => loop(t(), count - 1)
        case _                       => rest

    loop(this, n)

  /** 5.3: Write the function takeWhile for returning all starting elements of a LazyList that match the given predicate
    */
  def takeWhile(p: A => Boolean): LazyList[A] =
    @tailrec
    def loop(ll: LazyList[A], res: List[A]): LazyList[A] =
      ll match
        case Cons(h, t) if p(h()) => loop(t(), h() :: res)
        case _                    => LazyList(res.reverse*)

    loop(this, Nil)

  /** 5.4: Implement forAll, which checks that all elements in the LazyList match a given predicate. Your implementation should terminate the
    * traversal as soon as it encounters a non matching value.
    */
  def forAll(p: A => Boolean): Boolean =
    foldRight(true)((a, b) => p(a) && b)

  /** 5.5: Use foldRight to implement takeWhile. */
  def takeWhileF(p: A => Boolean): LazyList[A] =
    foldRight(LazyList.empty)((a, res) => if p(a) then LazyList.cons(a, res) else res)

  /** 5.6: Implement headOption using foldRight. */
  def headOptionF: Option[A] =
    foldRight(None: Option[A])((a, _) => Some(a))

  /** 5.7: Implement map, filter, append, and flatMap using foldRight. The append method should be non strict in its argument.
    */
  def map[B](f: A => B): LazyList[B] =
    foldRight(LazyList.empty)((a, b) => LazyList.cons(f(a), b))

  def filter(p: A => Boolean): LazyList[A] =
    takeWhileF(p)

  def append[B >: A](bs: => LazyList[B]): LazyList[B] =
    foldRight(bs)((a, res) => LazyList.cons(a, res))

  def flatMap[B](f: A => LazyList[B]): LazyList[B] =
    foldRight(LazyList.empty)((a, res) => f(a).append(res))

object LazyList:
  def cons[A](hd: => A, tl: => LazyList[A]): LazyList[A] =
    lazy val head = hd
    lazy val tail = tl

    Cons(() => head, () => tail)

  def empty[A]: LazyList[A] = Empty

  def apply[A](as: A*): LazyList[A] =
    if as.isEmpty then empty
    else cons(as.head, apply(as.tail*))

  /** 5.8: Generalize ones slightly to the function continually, which returns an infinite LazyList of a given value
    */
  def continually[A](a: A): LazyList[A] =
    cons(a, continually(a))

  /** Obs.: A more efficient implementation allocates a single Cons cell that references itself */
  def continuallyP[A](a: A): LazyList[A] =
    lazy val single: LazyList[A] = cons(a, single)
    single

  /** 5.9: Write a function that generates an infinite lazy list of integers starting from n, then n + 1, n + 2, and so on.
    */
  def from(n: Int): LazyList[Int] =
    cons(n, from(n + 1))

  /** 5.11: Write a more general LazyList-building function called unfold. It takes an initial state and a function for producing both the next state
    * and the next value in the generated lazy list:
    */
  def unfold[A, S](state: S)(f: S => Option[(A, S)]): LazyList[A] =
    f(state) match
      case Some((a, s)) => cons(a, unfold(s)(f))
      case _            => Empty
