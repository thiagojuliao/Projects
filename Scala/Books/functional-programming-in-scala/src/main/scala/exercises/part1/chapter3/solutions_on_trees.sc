import exercises.part1.chapter3.Tree
import exercises.part1.chapter3.Tree.*

/** 3.25: Write a function, maximum, that returns the maximum element in a Tree[Int]. (Note that in Scala you can use x.max(y) to compute the maximum
  * of two integers x and y.) Let’s try writing a few more functions.
  */
def maximum(t: Tree[Int]): Int = t match
  case Leaf(value)  => value
  case Branch(l, r) => maximum(l) max maximum(r)

val tree = Branch(Leaf(1), Branch(Leaf(7), Branch(Leaf(2), Leaf(4))))

maximum(tree)

/** 3.26: Write a function, depth, that returns the maximum path length from the root of a tree to any leaf.
  */
def depth[A](t: Tree[A]): Int = t match
  case Leaf(_)      => 0
  case Branch(l, r) => (1 + depth(l)) max (1 + depth(r))

depth(tree)

/** 3.27: Write a function, map, analogous to the method of the same name on List that modifies each element in a tree with a given function. */
def map[A, B](t: Tree[A])(f: A => B): Tree[B] = t match
  case Leaf(value)  => Leaf(f(value))
  case Branch(l, r) => Branch(map(l)(f), map(r)(f))

map(tree)(_ * 2)

/** 3.28: Generalize size, maximum, depth, and map, writing a new function, fold, that abstracts over their similarities. Reimplement them in terms of
  * this more general function. Can you draw an analogy between this fold function and the left and right folds for List?
  */
def fold[A, B](t: Tree[A])(f: A => B)(g: (B, B) => B): B = t match
  case Leaf(value)  => f(value)
  case Branch(l, r) => g(fold(l)(f)(g), fold(r)(f)(g))

def size_[A](t: Tree[A]): Int =
  fold(t)(_ => 1)(1 + _ + _)

size_(tree)

def maximum_(t: Tree[Int]): Int =
  fold(t)(identity)(_ max _)

maximum_(tree)

def depth_[A](t: Tree[A]): Int =
  fold(t)(_ => 0)((l, r) => (1 + l) max (1 + r))

depth_(tree)

def map_[A, B](t: Tree[A])(f: A => B): Tree[B] =
  fold(t)(a => Leaf(f(a)))(Branch(_, _))

map_(tree)(_ * 2)
