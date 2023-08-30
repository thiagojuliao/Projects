package exercises.part1.chapter3

enum Tree[+A]:
  case Leaf(value: A)                        extends Tree[A]
  case Branch(left: Tree[A], right: Tree[A]) extends Tree[A]

  def size: Int = this match
    case Leaf(_)      => 1
    case Branch(l, r) => 1 + l.size + r.size

object Tree:
  def apply[A](a: A): Tree[A] = Leaf(a)
