package exercises.part3.chapter12

case class Tree[+A](head: A, tail: List[Tree[A]])
