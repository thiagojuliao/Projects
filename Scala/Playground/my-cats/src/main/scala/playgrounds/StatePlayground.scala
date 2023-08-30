package playgrounds

import data.State
import syntax.all.*

object StatePlayground extends App {
  val nextInt = State[Int, Int](n => (n + 1, n))

  val test = for {
    n <- nextInt
    m <- nextInt
  } yield (n, m)

  test.run(0).show

  // Create a function to relabel a Tree using fresh integers
  sealed trait Tree[A]
  case class Leaf[A](a: A)                   extends Tree[A]
  case class Node[A](l: Tree[A], r: Tree[A]) extends Tree[A]

  def relabel[A](tree: Tree[A]): Tree[Int] = {
    def loop(tree: Tree[A]): State[Int, Tree[Int]] =
      tree match {
        case Leaf(_)    => nextInt.flatMap(n => State.pure(Leaf(n)))
        case Node(l, r) =>
          for {
            l_ <- loop(l)
            r_ <- loop(r)
          } yield Node(l_, r_)
      }

    loop(tree).runA(0)
  }

  val charTree: Tree[Char] = Node(
    Node(Leaf('a'), Leaf('b')),
    Leaf('c')
  )

  relabel(charTree).show
}
