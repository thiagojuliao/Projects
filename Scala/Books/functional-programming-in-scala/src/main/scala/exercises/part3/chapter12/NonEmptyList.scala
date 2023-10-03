package exercises.part3.chapter12

case class NonEmptyList[+A](head: A, tail: List[A]):
  def toList: List[A] = head :: tail

object NonEmptyList:
  def apply[A](head: A, tail: A*): NonEmptyList[A] =
    NonEmptyList(head, tail.toList)

  def fromList[A](ls: List[A]): NonEmptyList[A] =
    if ls.isEmpty then sys.error("Non empty lists cannot be empty")
    else NonEmptyList.apply(ls.head, ls.tail*)
