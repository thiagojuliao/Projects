import scala.annotation.tailrec
import scala.util.Random

/** P01 (*) Find the last element of a list. */
@tailrec
def last[A](ls: List[A]): A = ls match
  case last :: Nil => last
  case _           => last(ls.tail)

last(List(1, 1, 2, 3, 5, 8))

/** P02 (*) Find the last but one element of a list. */
@tailrec
def penultimate[A](ls: List[A]): A = ls match
  case pen :: _ :: Nil => pen
  case _               => penultimate(ls.tail)

penultimate(List(1, 1, 2, 3, 5, 8))

/** Find the Kth element of a list. */
def nth[A](n: Int, ls: List[A]): A =
  @tailrec
  def loop(i: Int, rest: List[A]): A =
    if i == 0 then rest.head
    else loop(i - 1, rest.tail)

  loop(n, ls)

nth(2, List(1, 1, 2, 3, 5, 8))

/** P05 (*) Reverse a list. */
def reverse[A](ls: List[A]): List[A] =
  @tailrec
  def loop(rest: List[A], acc: List[A]): List[A] =
    if rest.isEmpty then acc
    else loop(rest.tail, rest.head :: acc)

  loop(ls, Nil)

reverse(List(1, 1, 2, 3, 5, 8))

/** P06 (*) Find out whether a list is a palindrome. */
def isPalindrome[A](ls: List[A]): Boolean =
  ls == reverse(ls)

isPalindrome(List(1, 2, 3, 2, 1))
isPalindrome(List(1, 2, 3, 2, 5))

/** P07 (**) Flatten a nested list structure. */
/** Challenge: Implement a tailrec version */
def flatten(ls: List[Any]): List[Any] = ls match
  case Nil                => Nil
  case (as: List[_]) :: t => flatten(as) ::: flatten(t)
  case h :: t             => h :: flatten(t)

flatten(List(List(1, 1), 2, List(3, List(5, 8))))

/** P08 (**) Eliminate consecutive duplicates of list elements. */
def compress[A](ls: List[A]): List[A] =
  @tailrec
  def loop(value: A, rest: List[A], res: List[A]): List[A] =
    val rest_ = rest.dropWhile(_ == value)

    rest_ match
      case Nil    => res :+ value
      case h :: t => loop(h, t, res :+ value)

  loop(ls.head, ls.tail, Nil)

def compressF[A](ls: List[A]): List[A] =
  ls.foldRight(List.empty[A]) { (a, res) =>
    if res.isEmpty || a != res.head then a :: res
    else res
  }

compress(List(1, 1, 1, 2, 2, 2, 3, 4, 4, 4, 5, 6, 6))
compressF(List(1, 1, 1, 2, 2, 2, 3, 4, 4, 4, 5, 6, 6))

/** P09 (**) Pack consecutive duplicates of list elements into sublists. */
def pack[A](ls: List[A]): List[List[A]] =
  @tailrec
  def loop(value: A, rest: List[A], res: List[List[A]]): List[List[A]] =
    val packed = value :: rest.takeWhile(_ == value)
    val rest_  = rest.dropWhile(_ == value)

    rest_ match
      case Nil    => res :+ List(value)
      case h :: t => loop(h, t, res :+ packed)

  loop(ls.head, ls.tail, Nil)

pack(List(1, 1, 1, 2, 2, 1, 3, 4, 4, 5, 6, 5, 6))

/** P10 (*) Run-length encoding of a list. */
def encode[A](ls: List[A]): List[(Int, A)] =
  pack(ls).map(p => p.length -> p.head)

encode(List(1, 1, 1, 2, 2, 1, 3, 4, 4, 5, 6, 5, 6))

/** P11 (*) Modified run-length encoding. */
def encodeModified[A](ls: List[A]): List[Any] =
  for {
    e  <- encode(ls)
    res = if e._1 == 1 then e._2 else e
  } yield res

encodeModified(List(1, 1, 1, 2, 2, 1, 3, 4, 4, 5, 6, 5, 6))

/** P12 (**) Decode a run-length encoded list. */
def decode[A](enc: List[(Int, A)]): List[A] =
  for {
    e   <- enc
    res <- List.fill(e._1)(e._2)
  } yield res

decode(encode(List(1, 1, 1, 2, 2, 1, 3, 4, 4, 5, 6, 5, 6)))

/** P13 (**) Run-length encoding of a list (direct solution). */
def encodeDirect[A](ls: List[A]): List[(Int, A)] =
  @tailrec
  def loop(l: List[A], acc: List[A], res: List[(Int, A)]): List[(Int, A)] =
    l match
      case Nil    => res
      case h :: t =>
        if acc.isEmpty || h == acc.head then loop(t, h :: acc, res)
        else loop(t, h :: Nil, res :+ (acc.length -> acc.head))

  loop(ls, Nil, Nil)

encodeDirect(List(1, 1, 1, 2, 2, 1, 3, 4, 4, 5, 6, 5, 6))

/** P14 (*) Duplicate the elements of a list. */
def duplicate[A](ls: List[A]): List[A] =
  for {
    e   <- ls
    res <- e :: e :: Nil
  } yield res

duplicate(List("a", "b", "c", "c", "d"))

/** P15 (**) Duplicate the elements of a list a given number of times. */
def duplicateN[A](n: Int, ls: List[A]): List[A] =
  for {
    e   <- ls
    res <- List.fill(n)(e)
  } yield res

duplicateN(3, List("a", "b", "c", "d"))

/** P16 (**) Drop every Nth element from a list. */
def drop[A](n: Int, ls: List[A]): List[A] =
  @tailrec
  def loop(l: List[A], count: Int, res: List[A]): List[A] =
    l match
      case Nil    => res
      case h :: t =>
        if count == n then loop(t, 1, res)
        else loop(t, count + 1, res :+ h)

  loop(ls, 1, Nil)

drop(3, List("a", "b", "c", "d", "e", "f"))

/** P17 (*) Split a list into two parts. */
def split[A](n: Int, ls: List[A]): (List[A], List[A]) =
  ls.take(n) -> ls.drop(n)

split(3, List(1, 2, 3, 4, 5))

/** P18 (**) Extract a slice from a list. */
def slice[A](start: Int, end: Int, ls: List[A]): List[A] =
  ls.drop(start).take(end - (start max 0)) // max for cases where start < 0

slice(3, 7, List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"))

/** P19 (**) Rotate a list N places to the left. */
def rotate[A](n: Int, ls: List[A]): List[A] =
  if n >= 0 then ls.drop(n) ++ ls.take(n)
  else ls.reverse.take(n * -1) ++ ls.take(ls.length + n)

rotate(3, List("a", "b", "c", "d"))
rotate(-2, List("a", "b", "c", "d"))

/** P20 (*) Remove the Kth element from a list. */
def removeAt[A](n: Int, ls: List[A]): (List[A], Option[A]) =
  @tailrec
  def loop(l: List[A], count: Int, res: List[A]): (List[A], Option[A]) =
    l match
      case Nil => res -> None

      case h :: t if count == n => res ++ t -> Some(h)

      case h :: t => loop(t, count + 1, res :+ h)

  loop(ls, 0, Nil)

removeAt(1, List('a', 'b', 'c', 'd'))
removeAt(5, List('a', 'b', 'c', 'd'))

/** P21 (*) Insert an element at a given position into a list. */
def insertAt[A](e: A, n: Int, ls: List[A]): List[A] =
  @tailrec
  def loop(l: List[A], count: Int, res: List[A]): List[A] =
    l match
      case Nil             => res
      case _ if count == n => (res :+ e) ++ l
      case h :: t          => loop(t, count + 1, res :+ h)

  loop(ls, 0, Nil)

insertAt("new", 1, List("a", "b", "c", "d"))

/** P22 (*) Create a list containing all integers within a given range. */
def range(start: Int, end: Int): List[Int] =
  (start to end).toList

range(4, 9)

/** P23 (**) Extract a given number of randomly selected elements from a list. */
def randomSelect[A](n: Int, ls: List[A]): List[A] =
  type State = (List[A], List[A])
  val rnd = new Random()

  (1 to n)
    .foldLeft[State]((ls, Nil)) { case ((rest, choices), _) =>
      val (rest_, choice) =
        removeAt(rnd.nextInt(rest.length), rest)

      rest_ -> (choice.get :: choices)
    }
    ._2

randomSelect(3, List('a', 'b', 'c', 'd', 'f', 'g', 'h'))

/** P24 (*) Lotto: Draw N different random numbers from the set 1..M. */
def lotto(n: Int, m: Int): List[Int] =
  randomSelect(n, (1 to m).toList)

lotto(6, 49)

/** P25 (*) Generate a random permutation of the elements of a list. */
def randomPermute[A](ls: List[A]): List[A] =
  randomSelect(ls.length, ls)

randomPermute(List('a', 'b', 'c', 'd', 'f', 'g', 'h'))

/** P26 (**) Generate the combinations of K distinct objects chosen from the N elements of a list. */
def combinations[A](n: Int, ls: List[A]): List[List[A]] = ???
