package exercises.part3.chapter12.instances

import exercises.part3.chapter12.{Applicative, Traverse}

object map:
  /** 12.13: Write Traverse instances for List, Option, Tree, and [x] =>> Map[K, x]: */
  given [K]: Traverse[[V] =>> Map[K, V]] with
    override def traverse[G[_]: Applicative, A, B](ma: Map[K, A])(f: A => G[B]): G[Map[K, B]] =
      Applicative[G].sequenceMap(ma.view.mapValues(f).toMap)
