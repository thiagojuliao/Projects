package exercises.part1.chapter6

import scala.annotation.tailrec

trait RNG:
  def nextInt: (Int, RNG)

object RNG:

  /** 6.1: Write a function that uses RNG.nextInt to generate a random integer between 0 and Int.MaxValue (inclusive). Make sure to handle the corner
    * case when nextInt returns Int.MinValue, which doesn’t have a non negative counterpart.
    */
  def nonNegativeInt(rng: RNG): (Int, RNG) =
    val (int, newRNG) = rng.nextInt

    if int == Int.MinValue then 0     -> newRNG
    else Math.abs(int % Int.MaxValue) -> newRNG

  /** 6.2: Write a function to generate a Double between 0 and 1, not including 1. Note that you can use Int.MaxValue to obtain the maximum positive
    * integer value, and you can use x.toDouble to convert an x: Int to a Double.
    */
  def double(rng: RNG): (Double, RNG) =
    val (int, newRng) = rng.nextInt

    if int == Int.MinValue then 0.0         -> newRng
    else Math.abs(int * 1.0 / Int.MinValue) -> newRng

  /** 6.3: Write functions to generate an (Int, Double) pair, a (Double, Int) pair, and a (Double, Double, Double) 3-tuple. You should be able to
    * reuse the functions you’ve already written
    */
  def intDouble(rng: RNG): ((Int, Double), RNG) =
    val (int, rng1)    = nonNegativeInt(rng)
    val (double, rng2) = this.double(rng1)

    (int, double) -> rng2

  def doubleInt(rng: RNG): ((Double, Int), RNG) =
    val (double, rng1) = this.double(rng)
    val (int, rng2)    = nonNegativeInt(rng1)

    (double, int) -> rng2

  def double3(rng: RNG): ((Double, Double, Double), RNG) =
    val (double1, rng1) = double(rng)
    val (double2, rng2) = double(rng1)
    val (double3, rng3) = double(rng2)

    (double1, double2, double3) -> rng3

  /** 6.4: Write a function to generate a list of random integers. */
  def ints(count: Int)(rng: RNG): (List[Int], RNG) =
    @tailrec
    def generate(n: Int, rng: RNG, res: List[Int]): (List[Int], RNG) =
      if n == 0 then res -> rng
      else
        val (int, rng_) = rng.nextInt
        generate(n - 1, rng_, int :: res)

    generate(count, rng, Nil)

  /** Implemented for Part2.Chapter8 */
  def from(start: Int, end: Int)(rng: RNG): (Int, RNG) =
    val (a, rng_) = rng.nextInt
    start + a.abs % (end - start) -> rng_

  def boolean(rng: RNG): (Boolean, RNG) =
    val (n, rng_) = rng.nextInt

    if n >= 0 then true -> rng_
    else false          -> rng_
