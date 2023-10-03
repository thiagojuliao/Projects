package exercises.part3.chapter11

import exercises.part3.chapter12.Applicative

export Monad.syntax.*

trait Monad[F[_]] extends Applicative[F]:
  def unit[A](a: => A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  override def ap[A, B](fa: F[A])(fab: F[A => B]): F[B] =
    flatMap(fab)(f => flatMap(fa)(a => unit(f(a))))

  /** 11.3: The sequence and traverse combinators should be familiar to you by now, and your implementations of them from various prior chapters are
    * probably all very similar. Implement them once and for all on Monad[F]:
    */
  def sequence[A](fas: List[F[A]]): F[List[A]] =
    traverse(fas)(identity)

  def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldLeft(unit(Nil: List[B]))((acc, a) => map2(f(a), acc)(_ :: _))

  /** 11.4: Implement replicateM. */
  def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
    sequence(List.fill(n)(fa))

  /** 11.5: Think about how replicateM will behave for various choices of F. For example, how does it behave in the List monad? What about Option?
    * Describe in your own words the general meaning of replicateM.
    *
    * Ans: replicateM repeats the supplied monadic value n times, combining the results into a single value, where the monadic type defines how that
    * combination is performed.
    */

  /** 11.6: Hard: Here’s an example of a function we haven’t seen before. Implement the function filterM—it’s a bit like filter, except instead of a
    * function from A => Boolean, we have an A => F[Boolean]. (Replacing various ordinary functions like this with the monadic equivalent often yields
    * interesting results.) Implement this function, and then think about what it means for various data types
    */
  def filterM[A](as: List[A])(f: A => F[Boolean]): F[List[A]] =
    as.foldLeft(unit(Nil: List[A])) { (acc, a) =>
      map2(acc, f(a)) { (ls, b) =>
        if b then a :: ls else ls
      }
    }

  /** 11.7: Implement the Kleisli composition function compose. */
  def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] = a => flatMap(f(a))(g)

  /** 11.8: Hard: Implement flatMap in terms of compose. It seems we’ve found another minimal set of monad combinators: compose and unit.
    */
  def flatMap2[A, B](fa: F[A])(f: A => F[B]): F[B] =
    val g: Unit => F[A] = _ => fa
    compose(g, f)(())

  /** 11.12: There’s a third minimal set of monadic combinators: map, unit, and join. Implement join in terms of flatMap
    */
  def join[A](ffa: F[F[A]]): F[A] =
    flatMap(ffa)(identity)

  /** 11.13: Implement either flatMap or compose in terms of join and map. */
  def compose2[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => join(map(f(a))(b => g(b)))
end Monad

object Monad:
  def apply[F[_]](using monad: Monad[F]): Monad[F] = monad

  object syntax:
    extension [F[_]: Monad, A](fa: F[A])
      def flatMap[B](f: A => F[B]): F[B] = Monad[F].flatMap(fa)(f)

      def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
        Monad[F].map2(fa, fb)(f)

      def product[B](fb: F[B]): F[(A, B)] =
        Monad[F].map2(fa, fb)(_ -> _)
end Monad
