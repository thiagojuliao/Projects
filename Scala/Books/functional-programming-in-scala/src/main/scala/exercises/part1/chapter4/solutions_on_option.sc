import scala.annotation.tailrec
import exercises.part1.chapter4.Maybe
import exercises.part1.chapter4.Maybe.*

/** 4.2: Implement the variance function in terms of flatMap. If the mean of a sequence is m, the variance is the mean of math.pow(x - m, 2) for each
  * element x in the sequence.
  */
def mean(xs: Seq[Double]): Maybe[Double] =
  if xs.isEmpty then None else Just(xs.sum / xs.length)

def variance(xs: Seq[Double]): Maybe[Double] =
  mean(xs).flatMap(m => mean(xs.map(x => math.pow(x - m, 2))))

variance(Seq(21.3, 38.4, 12.7, 41.6))

/** 4.3.2 Option composition, lifting, and wrapping exception-oriented APIs */
def lift[A, B](f: A => B): Maybe[A] => Maybe[B] =
  _.map(f)

val abs0: Maybe[Double] => Maybe[Double] =
  lift(math.abs)

abs0(Just(-1.0))

/** 4.3: Write a generic function map2 that combines two Option values using a binary function. If either Option value is None, then the return value
  * is too
  */
def map2[A, B, C](ma: Maybe[A], mb: Maybe[B])(f: (A, B) => C): Maybe[C] =
  ma.flatMap(a => mb.map(b => f(a, b)))

map2(Maybe(3), Maybe(4))(_ + _)
map2(Maybe(3), None: Maybe[Int])(_ + _)

/** 4.4: Write a function sequence that combines a list of Options into one Option containing a list of all the Some values in the original list. If
  * the original list contains None even once, the result of the function should be None; otherwise, the result should be Some, with a list of all the
  * values.
  */
def sequence[A](xs: List[Maybe[A]]): Maybe[List[A]] =
  @tailrec
  def build(l: List[Maybe[A]], res: List[A]): Maybe[List[A]] =
    l match
      case Nil          => Just(res)
      case None :: _    => None
      case Just(a) :: t => build(t, res :+ a)

  build(xs, Nil)

sequence(List(Just(5), Just(2), Just(3)))
sequence(List(Just(1), None, Just(10)))

/** 4.5: Implement traverse and then implement sequence in terms of traverse */
def toIntMaybe(s: String): Maybe[Int] =
  try Just(s.toInt)
  catch case _ => None

def traverse[A, B](as: List[A])(f: A => Maybe[B]): Maybe[List[B]] =
  @tailrec
  def build(l: List[A], res: Maybe[List[B]]): Maybe[List[B]] =
    l match
      case Nil    => res
      case h :: t =>
        f(h) match
          case None    => None
          case Just(b) => build(t, res.map(_ :+ b))

  build(as, Just(Nil))

traverse(List("1", "2", "3"))(toIntMaybe)
traverse(List("1", "b", "3"))(toIntMaybe)

def sequence_[A](xs: List[Maybe[A]]): Maybe[List[A]] =
  traverse(xs)(identity)

sequence_(List(Just(5), Just(2), Just(3)))
sequence_(List(Just(1), None, Just(10)))
