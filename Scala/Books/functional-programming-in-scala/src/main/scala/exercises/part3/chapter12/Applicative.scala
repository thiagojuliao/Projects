package exercises.part3.chapter12

import exercises.part3.chapter11.Functor

trait Applicative[F[_]] extends Functor[F]:
  self =>

  /** 12.2: Hard: The name applicative comes from the fact that we can formulate the Applicative interface using an alternate set of primitives, unit
    * and the function apply, rather than unit and map2. Show that this formulation is equivalent in expressiveness by defining map2 and map in terms
    * of unit and apply. Also establish that apply can be implemented in terms of map2 and unit.
    */
  def unit[A](a: => A): F[A]
  def ap[A, B](fa: F[A])(fab: F[A => B]): F[B]

  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    ap(fa)(unit(f))

  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    val fbc = ap(fa)(unit(f.curried))
    ap(fb)(fbc)

  /** 12.3: The apply method is useful for implementing map3, map4, and so on, and the pattern is straightforward. Implement map3 and map4 using only
    * unit, apply, and the curried method available on functions
    */
  def map3[A, B, C, D](fa: F[A], fb: F[B], fc: F[C])(f: (A, B, C) => D): F[D] =
    val g: (A, B) => C => D = (a, b) => f.curried(a)(b)

    val fcd = map2(fa, fb)(g)
    ap(fc)(fcd)

  def map4[A, B, C, D, E](fa: F[A], fb: F[B], fc: F[C], fd: F[D])(f: (A, B, C, D) => E): F[E] =
    val g: (A, B, C) => D => E = (a, b, c) => f.curried(a)(b)(c)

    val fde = map3(fa, fb, fc)(g)
    ap(fd)(fde)

  /** 12.8: Just like we can take the product of two monoids A and B to get the monoid (A, B), we can take the product of two applicative functors.
    * Implement this function on the Applicative trait
    */
  def product[G[_]](G: Applicative[G]): Applicative[[x] =>> (F[x], G[x])] = new Applicative[[x] =>> (F[x], G[x])]:
    override def unit[A](a: => A): (F[A], G[A]) = self.unit(a) -> G.unit(a)

    override def ap[A, B](fa: (F[A], G[A]))(fab: (F[A => B], G[A => B])): (F[B], G[B]) =
      self.ap(fa._1)(fab._1) -> G.ap(fa._2)(fab._2)

  /** 12.9: Hard: Applicative functors also compose another way! If F[_] and G[_] are applicative functors, then so is F[G[_]]. Implement this
    * function on the Applicative trait:
    */
  def compose[G[_]](G: Applicative[G]): Applicative[[x] =>> F[G[x]]] = new Applicative[[x] =>> F[G[x]]]:
    override def unit[A](a: => A): F[G[A]] =
      val g: A => G[A] = a => G.unit(a)
      self.ap(self.unit(a))(self.unit(g))

    override def ap[A, B](fa: F[G[A]])(fab: F[G[A => B]]): F[G[B]] =
      self.map2(fa, fab)((g, h) => G.ap(g)(h))

  /** 12.12: On the Applicative trait, implement sequence over a Map rather than a List: */
  def sequenceMap[K, V](ofv: Map[K, F[V]]): F[Map[K, V]] =
    ofv.foldLeft(unit(Map[K, V]())) { case (fmp, (k, fv)) =>
      map2(fmp, fv)((mp, v) => mp + (k -> v))
    }

end Applicative

object Applicative:
  def apply[F[_]](using applicative: Applicative[F]): Applicative[F] = applicative

  /** 12.1: Move the implementations of sequence, traverse, replicateM, and product from Monad to Applicative, using only map2 and unit or methods
    * implemented in terms of them:
    */
  def sequence[F[_]: Applicative, A](fas: List[F[A]]): F[List[A]] =
    traverse(fas)(identity)

  def traverse[F[_]: Applicative, A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(Applicative[F].unit(Nil: List[B])) { (a, acc) =>
      Applicative[F].map2(f(a), acc)(_ :: _)
    }

  def replicateM[F[_]: Applicative, A](n: Int, fa: F[A]): F[List[A]] =
    sequence(List.fill(n)(fa))

  extension [F[_]: Applicative, A](fa: F[A])
    def product[B](fb: F[B]): F[(A, B)] =
      Applicative[F].map2(fa, fb)((a, b) => (a, b))

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      Applicative[F].map2(fa, fb)(f)

    def map3[B, C, D](fb: F[B], fc: F[C])(f: (A, B, C) => D): F[D] =
      Applicative[F].map3(fa, fb, fc)(f)
end Applicative
