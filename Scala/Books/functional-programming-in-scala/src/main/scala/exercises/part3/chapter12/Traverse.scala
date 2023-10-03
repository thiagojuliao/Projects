package exercises.part3.chapter12

import exercises.part3.chapter11.Functor
import exercises.part3.chapter12.instances.id.given

trait Traverse[F[_]] extends Functor[F]:
  self =>

  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

  def sequence[G[_]: Applicative, A](fag: F[G[A]]): G[F[A]] =
    traverse(fag)(identity)

  /** 12.14: Hard: Implement map in terms of traverse as a method on Traverse[F]. This establishes that Traverse is an extension of Functor and the
    * traverse function is a generalization of map (for this reason, we sometimes call these traversable functors). Note that when implementing map,
    * you can call traverse with your choice of Applicative[G]:
    */
  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    val ff: A => Id[B] = a => Applicative[Id].unit(f(a))
    traverse(fa)(ff)

  /** 12.18: Use applicative functor products to write the fusion of two traversals. This function will, given two functions f and g, traverse fa a
    * single time, collecting the results of both functions at once.
    */
  def fuse[M[_]: Applicative, N[_]: Applicative, A, B](fa: F[A])(f: A => M[B], g: A => N[B]): (M[F[B]], N[F[B]]) =
    // productAp :: (F[A], G[A]) -> (F[A -> B], G[A -> B]) -> (F[B], G[B])
    val prodApp = Applicative[M].product(Applicative[N])
    traverse[[x] =>> (M[x], N[x]), A, B](fa)(a => (f(a), g(a)))(using prodApp)

  /** 12.19: Implement the composition of two Traverse instances. */
  def compose[G[_]: Traverse]: Traverse[[x] =>> F[G[x]]] = new Traverse[[x] =>> F[G[x]]]:
    override def traverse[H[_]: Applicative, A, B](fga: F[G[A]])(f: A => H[B]): H[F[G[B]]] =
      self.traverse(fga)(ga => Traverse[G].traverse(ga)(f))
end Traverse

object Traverse:
  def apply[F[_]](using traverse: Traverse[F]): Traverse[F] = traverse
