package exercises.part1.chapter3

import scala.annotation.tailrec

enum MyList[+A]:
  case Nil
  case Cons(h: A, t: MyList[A])

  override def toString: String =
    @tailrec
    def build(e: MyList[A], res: String): String = e match
      case Nil        => res + "Nil"
      case Cons(h, t) => build(t, res + s"$h::")

    build(this, "")

object MyList:
  def apply[A](as: A*): MyList[A] =
    if as.isEmpty then Nil
    else Cons(as.head, apply(as.tail*))
