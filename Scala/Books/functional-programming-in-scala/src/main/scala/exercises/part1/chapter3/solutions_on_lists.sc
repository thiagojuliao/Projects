import scala.annotation.tailrec
import exercises.part1.chapter3.MyList
import exercises.part1.chapter3.MyList.*

/** 3.2: Implement the function tail for removing the first element of a List (note that the function takes constant time). You can use
  * sys.error("message") to throw an exception if the List is Nil. In the next chapter, we’ll look at different ways of handling errors. Be careful to
  * use the List enum and the Nil case defined here and not the built-in Scala List and Nil types.
  */
def tail[A](as: MyList[A]): MyList[A] = as match
  case Nil        => sys.error("empty list")
  case Cons(_, t) => t

val ls = MyList(1, 2, 3, 4, 5)

tail(ls)

def safeTail[A](as: MyList[A]): Option[MyList[A]] = as match
  case Nil        => None
  case Cons(_, t) => Some(t)

safeTail(ls)

/** 3.3: Using the same idea, implement the function setHead for replacing the first element of a List with a different value. */
def setHead[A](a: A, as: MyList[A]): MyList[A] = as match
  case Nil => MyList(a)
  case _   => Cons(a, as)

setHead(10, ls)

/** 3.4: Implement the function drop, which removes the first n elements from a list. Dropping n elements from an empty list should return the empty
  * list. Note that this function takes time proportional only to the number of elements being dropped—we don’t need to make a copy of the entire List
  */
def drop[A](n: Int, as: MyList[A]): MyList[A] =
  @tailrec
  def loop(res: MyList[A], count: Int): MyList[A] =
    res match
      case Nil                     => Nil
      case Cons(_, t) if count > 0 =>
        loop(t, count - 1)
      case _                       => res

  loop(as, n)

drop(5, Nil)
drop(3, ls)

/** 3.5: Implement dropWhile, which removes elements from the List prefix as long as they match a predicate */
@tailrec
def dropWhile[A](as: MyList[A])(f: A => Boolean): MyList[A] = as match
  case Nil                => Nil
  case Cons(h, t) if f(h) => dropWhile(t)(f)
  case _                  => as

dropWhile(ls)(_ < 3)

/** 3.6: Not everything works out so nicely. Implement a function, init, that returns a List consisting of all but the last element of a List, so
  * given List(1,2,3,4), init will return List(1,2,3). Why can’t this function be implemented in constant time (that is, runtime that’s proportional
  * to the size of the list) like tail?
  */
def append[A](a1: MyList[A], a2: MyList[A]): MyList[A] =
  a1 match
    case Nil        => a2
    case Cons(h, t) => Cons(h, append(t, a2))

def init[A](as: MyList[A]): MyList[A] =
  @tailrec
  def loop(l: MyList[A], res: MyList[A]): MyList[A] =
    l match
      case Nil                   => Nil
      case Cons(h, Cons(_, Nil)) => append(res, MyList(h))
      case Cons(h, t)            => loop(t, append(res, MyList(h)))

  loop(as, Nil)

init(ls)

/** Listing 3.2 Right folds and simple uses */
def foldRight[A, B](as: MyList[A], acc: B)(f: (A, B) => B): B =
  as match
    case Nil        => acc
    case Cons(h, t) => f(h, foldRight(t, acc)(f))

foldRight(ls, 0)(_ + _)
foldRight(ls, 1)(_ * _)

/** 3.9: Compute the length of a list using foldRight */
def length[A](as: MyList[A]): Int =
  foldRight(as, 0)((_, acc) => acc + 1)

length(ls)

/** 3.10: Our implementation of foldRight is not tail recursive and will result in a StackOverflowError for large lists (we say it’s not stack safe).
  * Convince yourself that this is the case, and then write another general list-recursion function, foldLeft, that is tail recursive, using the
  * techniques we discussed in the previous chapter. Start collapsing from the leftmost start of the list
  */
@tailrec
def foldLeft[A, B](as: MyList[A], acc: B)(f: (B, A) => B): B =
  as match
    case Nil        => acc
    case Cons(h, t) => foldLeft(t, f(acc, h))(f)

/** 3.11: Write sum, product, and a function to compute the length of a list using foldLeft */
def sum(as: MyList[Int]): Int =
  foldLeft(as, 0)(_ + _)

def product(as: MyList[Int]): Int =
  foldLeft(as, 1)(_ * _)

def length_[A](as: MyList[A]): Int =
  foldLeft(as, 0)((acc, _) => acc + 1)

sum(ls)
product(ls)
length_(ls)

/** 3.11: Write a function that returns the reverse of a list (i.e., given List(1,2,3), it returns List(3,2,1)). See if you can write it using a fold
  */
def reverse[A](as: MyList[A]): MyList[A] =
  foldLeft(as, Nil: MyList[A])((state, e) => Cons(e, state))

reverse(ls)

/** 3.12: Hard: Can you write foldRight in terms of foldLeft? How about the other way around? Implementing foldRight via foldLeft is useful because it
  * lets us implement foldRight tail recursively, which means it works even for large lists without overflowing the stack.
  */
def swap[A, B, C](f: (A, B) => C): (B, A) => C =
  (b, a) => f(a, b)

def foldRightRec[A, B](as: MyList[A], acc: B)(f: (A, B) => B): B =
  foldLeft(reverse(as), acc)(swap(f))

foldRightRec(ls, Nil: MyList[Int])(Cons(_, _))

/** 3.14: Implement append in terms of either foldLeft or foldRight instead of structural recursion.
  */
def append_[A](a1: MyList[A], a2: MyList[A]): MyList[A] =
  foldLeft(reverse(a1), a2)((state, e) => setHead(e, state))

append_(MyList(1, 2, 3), MyList(4, 5, 6))

/** 3.15: Hard: Write a function that concatenates a list of lists into a single list. Its runtime should be linear in the total length of all lists.
  * Try to use functions we have already defined.
  */
def concat[A](ass: MyList[MyList[A]]): MyList[A] =
  foldLeft(ass, Nil: MyList[A])((state, as) => append_(state, as))

concat(MyList(MyList(1, 2), MyList(3, 4, 5)))

/** 3.16: Write a function that transforms a list of integers by adding 1 to each element (that is, given a list of integers, it returns a new list of
  * integers where each value is one more than the corresponding value in the original list)
  */
def add1(as: MyList[Int]): MyList[Int] =
  foldRightRec(as, Nil: MyList[Int])((e, state) => setHead(e + 1, state))

add1(ls)

/** 3.17: Write a function that turns each value in a List[Double] into a String. You can use the expression d.toString to convert some d: Double to a
  * String.
  */
def stringify(ds: MyList[Double]): MyList[String] =
  foldRightRec(ds, Nil: MyList[String])((e, state) => setHead(e.toString, state))

stringify(MyList(0.0, 1.53, 7.68))

/** 3.18: Write a function, map, that generalizes modifying each element in a list while maintaining the structure of the list */
def map[A, B](as: MyList[A])(f: A => B): MyList[B] =
  foldRightRec(as, Nil: MyList[B])((a, state) => setHead(f(a), state))

map(ls)(_ + 2)

/** 3.19: Write a function, filter, that removes elements from a list unless they satisfy a given predicate. Use it to remove all odd numbers from a
  * List[Int]
  */
def filter[A](as: MyList[A])(predicate: A => Boolean): MyList[A] =
  foldRightRec(as, Nil: MyList[A]) { (a, state) =>
    if predicate(a) then setHead(a, state) else state
  }

filter(ls)(_ % 2 == 0)

/** 3.20: Write a function, flatMap, that works like map except that the function given will return a list instead of a single result, ensuring that
  * the list is inserted into the final resulting list
  */
def flatMap[A, B](as: MyList[A])(f: A => MyList[B]): MyList[B] =
  foldRightRec(as, Nil: MyList[B]) { (a, state) =>
    append_(f(a), state)
  }

flatMap(ls)(a => MyList(a, a + 1))

/** 3.21: Use flatMap to implement filter. */
def filter_[A](as: MyList[A])(predicate: A => Boolean): MyList[A] =
  val filterF = (a: A) => if predicate(a) then MyList(a) else Nil
  flatMap(as)(filterF)

filter_(ls)(_ % 2 == 0)

/** 3.22: Write a function that accepts two lists and constructs a new list by adding corresponding elements. For example, List(1,2,3) and List(4,5,6)
  * become List(5,7,9)
  */
def zip[A, B](as: MyList[A], bs: MyList[B]): MyList[(A, B)] =
  @tailrec
  def loop(l1: MyList[A], l2: MyList[B], res: MyList[(A, B)]): MyList[(A, B)] =
    (l1, l2) match
      case Nil -> Nil                   => res
      case _ -> Nil                     => res
      case Nil -> _                     => res
      case Cons(h1, t1) -> Cons(h2, t2) => loop(t1, t2, append_(res, MyList(h1 -> h2)))

  loop(as, bs, Nil)

zip(MyList(1, 2, 3), MyList(4, 5, 6))

def combineIntLists(a1: MyList[Int], a2: MyList[Int]): MyList[Int] =
  map(zip(a1, a2))((a1, a2) => a1 + a2)

combineIntLists(MyList(1, 2, 3), MyList(4, 5, 6))

/** 3.23: Generalize the function you just wrote so it’s not specific to integers or addition. */
def zipWith[A, B, C](as: MyList[A], bs: MyList[B])(f: (A, B) => C): MyList[C] =
  map(zip(as, bs))((a, b) => f(a, b))

zipWith(MyList(1, 2, 3), MyList(4, 5, 6))(_ + _)

/** 3.24: Hard: Implement hasSubsequence to check whether a List contains another List as a subsequence. For instance, List(1,2,3,4) would have
  * List(1,2), List(2,3), and List(4) as subsequences, among others. You may have some difficulty finding a concise and purely functional
  * implementation that is also efficient—that’s OK. Implement the function in whatever manner comes most naturally. We’ll return to this
  * implementation in chapter 5 and hopefully improve upon it. Note that any two values x and y can be compared for equality in Scala using the
  * expression x == y.
  */
@tailrec
def forAll[A](as: MyList[A])(f: A => Boolean): Boolean =
  as match
    case Nil                 => true
    case Cons(h, _) if !f(h) => false
    case Cons(_, t)          => forAll(t)(f)

forAll(ls)(_ > 0)
forAll(ls)(_ < 5)

@tailrec
def contains[A](a: A, as: MyList[A]): Boolean =
  as match
    case Nil                  => false
    case Cons(h, _) if h == a => true
    case Cons(_, t)           => contains(a, t)

contains(5, ls)

def hasSubsequence[A](sup: MyList[A], sub: MyList[A]): Boolean =
  sub match
    case Nil => true
    case _   => forAll(sub)(a => contains(a, sup))

hasSubsequence(ls, Nil)
hasSubsequence(ls, MyList(1, 3, 5))
