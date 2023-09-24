package exercises.part3.chapter11

/** 11.20: Hard: To cement your understanding of monads, give a monad instance for the following type, and explain what it means. What are its
  * primitive operations? What is the action of flatMap? What meaning does it give to monadic functions like sequence, join, and replicateM? What
  * meaning does it give to the monad laws?
  */
opaque type Reader[-R, +A] = R => A

object Reader:
  def ask[R]: Reader[R, R] = r => r

  def apply[R, A](f: R => A): Reader[R, A] = f

  given [R]: Monad[[A] =>> Reader[R, A]] with
    override def unit[A](a: => A): Reader[R, A] = _ => a

    override def flatMap[A, B](fa: Reader[R, A])(f: A => Reader[R, B]): Reader[R, B] = r => f(fa(r))(r)
