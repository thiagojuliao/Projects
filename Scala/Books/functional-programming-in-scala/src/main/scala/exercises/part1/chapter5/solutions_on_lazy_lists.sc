import exercises.part1.chapter5.LazyList
import exercises.part1.chapter5.LazyList.*

import scala.annotation.tailrec

/** Tests - Lazy List API */
val ll = LazyList(1, 2, 3, 4, 5)
ll.toList

// An expensive computation that takes to long to complete
def compute[A](a: => A, sec: Int): A = { Thread.sleep(sec * 1000); a }
cons(1, cons(compute(42, 10), Empty)).headOption

cons(1, cons(2, cons(compute(42, 2), Empty))).take(2)
cons(1, cons(2, cons(compute(42, 2), Empty))).drop(2)

/** 5.5 */
ll.takeWhile(_ < 3).toList
ll.takeWhileF(_ < 3).toList

/** 5.6 */
ll.headOptionF
empty[Int].headOptionF

/** 5.7 */
ll.map(_ * 2).toList
ll.filter(_ % 2 == 0).toList
ll.append(ll).take(7).toList
ll.flatMap(a => LazyList(a, a + 1)).take(6).toList

LazyList(1, 2, 3, 4).map(_ + 10).filter(_ % 2 == 0).toList

/** Infinite Lazy Lists example */
val ones: LazyList[Int] = LazyList.cons(1, ones)

ones.take(5).toList
ones.exists(_ % 2 != 0)

/** 5.8 */
LazyList.continually(42).take(10).toList

/** 5.9 */
LazyList.from(1).take(100).toList // Generates the first 100 natural numbers starting from 1

/** 5.10: Write a function fibs that generates the infinite lazy list of Fibonacci numbers: 0, 1, 1, 2, 3, 5, 8, and so on
  */
def fibs: LazyList[Int] =
  def build(prev: Int, curr: Int): LazyList[Int] =
    cons(prev + curr, build(curr, prev + curr))

  LazyList(0, 1).append(build(0, 1))

fibs.take(10).toList

/** 5.12 */
def fibs2: LazyList[Int] =
  LazyList(0, 1).append(unfold((0, 1)) { (prev, curr) =>
    Some(prev + curr -> (curr, prev + curr))
  })

fibs2.take(10).toList

def from2(n: Int): LazyList[Int] =
  unfold(n)(s => Some((s, s + 1)))

from2(1).take(10).toList

def continually2[A](a: A): LazyList[A] =
  unfold(a)(s => Some(s, s))

continually2(1).take(5).toList

def ones2: LazyList[Int] = continually2(1)

/** 5.13 */
def map[A, B](as: LazyList[A])(f: A => B): LazyList[B] =
  unfold(as) {
    case Empty      => None
    case Cons(h, t) => Some(f(h()), t())
  }

map(ll)(_ * 2).toList

def take[A](as: LazyList[A], n: Int): LazyList[A] =
  unfold((as, n)) { (state, count) =>
    state match
      case Empty                   => None
      case Cons(h, t) if count > 0 => Some((h(), (t(), count - 1)))
      case _                       => None
  }

take(ll, 3).toList

def takeWhile[A](as: LazyList[A])(p: A => Boolean): LazyList[A] =
  unfold(as) {
    case Empty                => None
    case Cons(h, t) if p(h()) => Some((h(), t()))
    case _                    => None
  }

takeWhile(LazyList.from(1))(_ < 13).toList

def zipWith[A, B, C](as: LazyList[A], bs: LazyList[B])(f: (A, B) => C): LazyList[C] =
  unfold((as, bs)) {
    case Empty -> _                   => None
    case _ -> Empty                   => None
    case Cons(h1, t1) -> Cons(h2, t2) =>
      Some((f(h1(), h2()), (t1(), t2())))
  }

zipWith(LazyList.from(1), LazyList.from(2))(_ + _).take(5).toList

def zipAll[A, B](as: LazyList[A], bs: LazyList[B]): LazyList[(Option[A], Option[B])] =
  unfold((as, bs)) {
    case Empty -> Empty               => None
    case Empty -> Cons(h, t)          => Some((None, Some(h())), (Empty, t()))
    case Cons(h, t) -> Empty          => Some((Some(h()), None), (t(), Empty))
    case Cons(h1, t1) -> Cons(h2, t2) => Some((Some(h1()), Some(h2())), (t1(), t2()))
  }

zipAll(LazyList.from(1), LazyList.from(2).take(3)).take(5).toList

/** 5.14: Hard: Implement startsWith using functions you’ve written. It should check if one LazyList is a prefix of another. For instance,
  * LazyList(1,2,3).startsWith(LazyList (1,2)) would be true.
  */
@tailrec
def startsWith[A](as: LazyList[A], prefix: LazyList[A]): Boolean =
  as -> prefix match
    case Empty -> Empty                               => true
    case Empty -> Cons(_, _)                          => false
    case Cons(_, _) -> Empty                          => true
    case Cons(h1, t1) -> Cons(h2, t2) if h1() == h2() => startsWith(t1(), t2())
    case _                                            => false

def startsWith2[A](as: LazyList[A], prefix: LazyList[A]): Boolean =
  zipWith(as, prefix)(_ == _).forAll(identity)

startsWith(LazyList(1, 2, 3), LazyList(1, 2))
startsWith2(LazyList(1, 2, 3), LazyList(1, 2))

/** 5.15: Implement tails using unfold. For a given LazyList, tails returns the LazyList of suffixes of the input sequence, starting with the original
  * LazyList. For example, given LazyList(1, 2, 3), it would return LazyList(LazyList(1, 2, 3), LazyList(2, 3), LazyList(3), and LazyList()).
  */
def tails[A](as: LazyList[A]): LazyList[LazyList[A]] =
  unfold(as) {
    case Empty           => None
    case ls @ Cons(_, t) => Some((ls, t()))
  }.append(LazyList(LazyList()))

tails(LazyList(1, 2, 3)).map(_.toList).toList

/** Implementation of hasSubsequence using lazy list functions */
def hasSubsequence[A](as: LazyList[A], sub: LazyList[A]): Boolean =
  tails(as).exists(startsWith2(_, sub))

hasSubsequence(LazyList(1, 2, 3), LazyList(1, 2))

/** 5.16: Hard: Generalize tails to the function scanRight, which is like a foldRight that returns a lazy list of the intermediate results.
  */
def scanRight[A, B](as: LazyList[A], init: B)(combine: (A, => B) => B): LazyList[B] =
  unfold((as, init)) {
    case Empty -> _              => None
    case state @ Cons(_, t) -> _ =>
      val value = state._1.foldRight(init)(combine)
      Some((value, (t(), init)))
  }.append(LazyList(init))

scanRight(LazyList(1, 2, 3), 0)(_ + _).toList

def tails2[A](as: LazyList[A]): LazyList[LazyList[A]] =
  scanRight(as, empty[A])(cons)

tails2(LazyList(1, 2, 3)).map(_.toList).toList

/** Example: Create a primes function to retrieve the first n prime numbers. */
def sieve[A](as: => LazyList[A])(p: (A, A) => Boolean): LazyList[A] =
  as match
    case Empty      => Empty
    case Cons(h, t) => cons(h(), sieve(t().filter(p(_, h())))(p))

def primes: LazyList[Int] =
  sieve(LazyList.from(2))(_ % _ != 0)

primes.take(10).toList
