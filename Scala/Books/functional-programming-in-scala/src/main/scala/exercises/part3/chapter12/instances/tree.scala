package exercises.part3.chapter12.instances

import exercises.part3.chapter12.Tree
import exercises.part3.chapter12.{Applicative, Traverse}

object tree:
  /** 12.13: Write Traverse instances for List, Option, Tree, and [x] =>> Map[K, x]: */
  given Traverse[Tree] with
    override def traverse[G[_]: Applicative, A, B](ta: Tree[A])(f: A => G[B]): G[Tree[B]] =
      val Tree(h, t) = ta
      val gh         = f(h)
      val gls        = Applicative.sequence(t.map(traverse(_)(f)))

      Applicative[G].map2(gh, gls)(Tree.apply)
