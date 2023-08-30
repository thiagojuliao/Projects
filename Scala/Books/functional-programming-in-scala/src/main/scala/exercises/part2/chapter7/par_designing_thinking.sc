/** 7.1.1 A data type for parallel computations */
trait Par[A]

object Par {

  /** unit: Promotes a constant value to a parallel computation */
  def unit[A](a: A): Par[A] = ???

  def get[A](pa: Par[A]): A = ???

  /** run: Extracts a value from a Par by performing the computation */
  def run[A](pa: Par[A]): A = ???

  /** 7.1: Par.map2 is a new higher-order function for combining the result of two parallel computations. What is its signature? Give the most general
    * signature possible (without assuming it works only for Int).
    */
  /** map2: Combines the results of two parallel computations with a binary function */
  def map2[A, B, C](pa: Par[A], pb: Par[B])(f: (A, B) => C): Par[C] = ???

  /** 7.1.3 Explicit forking */
  /** fork: Marks a computation for concurrent evaluation—the evaluation won’t occur until forced by run
    */
  def fork[A](pa: => Par[A]): Par[A] = ???

  /** lazyUnit: Wraps its unevaluated argument in a Par and marks it for concurrent evaluation
    */
  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))
}

extension [A](pa: Par[A])
  def run: A = Par.run(pa)

  def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
    Par.map2(pa, pb)(f)

/** Listing 7.2 Updating sum with our custom data type */
def sum(ints: IndexedSeq[Int]): Int =
  if ints.size <= 1 then ints.headOption.getOrElse(0)
  else
    val (l, r)         = ints.splitAt(ints.size / 2)
    val sumL: Par[Int] = Par.unit(sum(l))
    val sumR: Par[Int] = Par.unit(sum(r))
    Par.get(sumL) + Par.get(sumR)

/** 7.1 */
def sum2(ints: IndexedSeq[Int]): Par[Int] =
  if ints.size <= 1 then Par.unit(ints.headOption.getOrElse(0))
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.map2(sum2(l), sum2(r))(_ + _)

/** 7.13 */
def sum3(ints: IndexedSeq[Int]): Par[Int] =
  if ints.size <= 1 then Par.unit(ints.headOption.getOrElse(0))
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.map2(Par.fork(sum3(l)), Par.fork(sum3(r)))(_ + _)
