package algebra

trait Applicative[F[_]] extends Functor[F]:
  /** Lifts a value `a` to the context of `F`.
    */
  def pure[A](a: A): F[A]

  /** Sequential application.
    */
  def ap[A, B](fa: F[A])(fab: F[A => B]): F[B]

  /** Sequence actions, discarding the value of the first argument.
    */
  def keepRight[A, B](fa: F[A], fb: F[B]): F[B] =
    val ff = map(fa)(_ => (b: B) => b)
    ap(fb)(ff)

  /** Sequence actions, discarding the value of the second argument.
    */
  def keepLeft[A, B](fa: F[A], fb: F[B]): F[A] =
    map2(fa, fb)((a, _) => a)

  /** See `map` on `Functor`.
    */
  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    ap(fa)(pure(f))

  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    val ff = pure(f.curried)
    ap(fb)(ap(fa)(ff))

object Applicative:
  /** Summons a given instance of `Applicative[F]` into scope.
    */
  def apply[F[_]](using applicative: Applicative[F]): Applicative[F] =
    applicative

  object syntax:
    extension [F[_]: Applicative, A](fa: F[A])
      def <*>[B](fab: F[A => B]): F[B] = Applicative[F].ap(fa)(fab)

      def *>[B](fb: F[B]): F[B] = Applicative[F].keepRight(fa, fb)

      def <*[B](fb: F[B]): F[A] = Applicative[F].keepLeft(fa, fb)
