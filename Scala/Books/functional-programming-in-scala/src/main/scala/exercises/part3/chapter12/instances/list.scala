package exercises.part3.chapter12.instances

import exercises.part3.chapter12.{Applicative, Traverse}

object list:
  /** 12.13: Write Traverse instances for List, Option, Tree, and [x] =>> Map[K, x]: */
  given Traverse[List] with
    override def traverse[G[_]: Applicative, A, B](fa: List[A])(f: A => G[B]): G[List[B]] =
      Applicative.traverse(fa)(f)
