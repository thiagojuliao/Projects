import exercises.part1.chapter6.RNG
import exercises.part1.chapter6.RNG.*
import exercises.part1.chapter6.SimpleRNG

import scala.annotation.tailrec

/** 6.1 */
val simpleRNG = SimpleRNG(196)
nonNegativeInt(simpleRNG)

/** 6.12 */
double(simpleRNG)

/** 6.13 */
intDouble(simpleRNG)
doubleInt(simpleRNG)
double3(simpleRNG)

/** 6.14 */
ints(5)(simpleRNG)

/** Type Alias for State Transactions on Random Number Generators */
type Rand[+A] = RNG => (A, RNG)

val int: Rand[Int] = _.nextInt

def unit[A](a: A): Rand[A] =
  rng => (a, rng)

def map[A, B](s: Rand[A])(f: A => B): Rand[B] =
  state =>
    val (a, rng) = s(state)
    (f(a), rng)

def nonNegativeEven: Rand[Int] =
  map(nonNegativeInt)(i => i - (i % 2))

/** 6.5: Use map to reimplement double in a more succinct way. See exercise 6.2. */
def doubleF(rng: RNG): (Double, RNG) =
  map(_.nextInt)(_.toDouble)(rng)

doubleF(simpleRNG)

/** 6.6: Write the implementation of map2 based on the following signature. This function takes two actions, ra and rb, and a function, f, for
  * combining their results and returns a new action that combines them
  */
def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
  state =>
    val (a, rng1) = ra(state)
    val (b, rng2) = rb(rng1)
    f(a, b) -> rng2

map2(int, doubleF)((_, _))(simpleRNG) // intDouble
map2(doubleF, int)((_, _))(simpleRNG) // doubleInt

def both[A, B](ra: Rand[A], rb: Rand[B]): Rand[(A, B)] =
  map2(ra, rb)((_, _))

val randIntDouble: Rand[(Int, Double)] = both(int, double)
val randDoubleInt: Rand[(Double, Int)] = both(double, int)

/** 6.7: Hard: If you can combine two RNG actions, you should be able to combine an entire list of them. Implement sequence for combining a List of
  * actions into a single action. Use it to reimplement the ints function you wrote before. For the latter, you can use the standard library function
  * List.fill(n)(x) to make a list with x repeated n times.
  */
def sequence[A](rs: List[Rand[A]]): Rand[List[A]] =
  state =>
    @tailrec
    def build(rest: List[Rand[A]], state: RNG, res: List[A]): (List[A], RNG) =
      rest match
        case Nil     => res -> state
        case st :: t =>
          val (a, s2) = st(state)
          build(t, s2, a :: res)

    build(rs, state, Nil)

sequence(List.fill(3)(int))(simpleRNG)

def sequence2[A](rs: List[Rand[A]]): Rand[List[A]] =
  map(
    rs.foldLeft(unit(Nil: List[A]))((acc, r) => map2(r, acc)(_ :: _))
  )(_.reverse)

sequence2(List.fill(3)(int))(simpleRNG)

def ints2(count: Int): Rand[List[Int]] =
  sequence2(List.fill(count)(int))

/** 6.8: Implement flatMap, and then use it to implement nonNegativeLessThan . */
def flatMap[A, B](ra: Rand[A])(f: A => Rand[B]): Rand[B] =
  state =>
    val (a, rng1) = ra(state)
    f(a)(rng1)

def nonNegativeLessThan(n: Int): Rand[Int] =
  flatMap(nonNegativeInt) { a =>
    val mod = a % n

    if a + (n - 1) - mod >= 0 then unit(mod)
    else nonNegativeLessThan(n)
  }

/** 6.9: Reimplement map and map2 in terms of flatMap. The fact that this is possible is what we’re referring to when we say that flatMap is more
  * powerful than map and map2.
  */
def mapF[A, B](ra: Rand[A])(f: A => B): Rand[B] =
  flatMap(ra)(a => unit(f(a)))

map(int)(_ * 2)(simpleRNG) == mapF(int)(_ * 2)(simpleRNG)

def map2F[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
  flatMap(ra)(a => map(rb)(b => f(a, b)))

map2(int, doubleF)((_, _))(simpleRNG) == map2F(int, doubleF)((_, _))(simpleRNG)

def rollDie: Rand[Int] = map(nonNegativeLessThan(6))(_ + 1)
rollDie(SimpleRNG(5))._1
