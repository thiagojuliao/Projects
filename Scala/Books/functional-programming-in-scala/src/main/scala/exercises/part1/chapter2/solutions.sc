import scala.annotation.tailrec
import exercises.extensions.*

/** Tail Recursive Fibonacci */
def fib(n: Int): BigInt =
  @tailrec
  def rec(n: Int, n_2: BigInt, n_1: BigInt): BigInt =
    if n <= 2 then n_1
    else rec(n - 1, n_1, n_2 + n_1)

  if n == 1 then 0
  else if n == 2 then 1
  else rec(n, 0, 1)

(1 to 15).map(fib).mkString(" ").show

/** Function that verifies whether a generic array is sorted by a given sorting function */
def isSorted[A](as: Array[A], gt: (A, A) => Boolean): Boolean =
  as.zip(as.tail).forall((x, y) => gt(y, x))

isSorted(Array(1, 2, 3), _ > _).show // true
isSorted(Array(1, 2, 1), _ > _).show // false
isSorted(Array(3, 2, 1), _ < _).show // true
isSorted(Array(1, 2, 3), _ < _).show // false

/** The currying function implementation */
def curry[A, B, C](f: (A, B) => C): A => B => C =
  a => b => f(a, b)

/** The uncurrying function implementation */
def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)

val add: (Int, Int) => Int = (x: Int, y: Int) => x + y
val add1: Int => Int       = curry(add)(1)

add1(5).show // 6

val add_ : (Int, Int) => Int = uncurry(curry(add))

assert(add(5, 3) == add_(5, 3)) // true

/** Function Composition */
def compose[A, B, C](f: B => C, g: A => B): A => C =
  a => f(g(a))

val add5: Int => Int   = _ + 5
val times2: Int => Int = _ * 2

assert(compose(add5, times2)(5) == 15) // compose(add5, times2)(5) = add5(times2(5)) = add5(10) = 10 + 5 = 15
