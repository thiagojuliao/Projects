package exercises.part3.chapter11.instances

import exercises.part1.chapter6.State
import exercises.part3.chapter11.Monad

object state:

  /** 11.2: Hard: State looks like it would be a monad too, but it takes two type arguments, and you need a type constructor of one argument to
    * implement Monad. Try to implement a State monad, see what problems you run into, and think about possible solutions. We’ll discuss the solution
    * later in this chapter.
    */
  given [S]: Monad[[A] =>> State[S, A]] with
    override def unit[A](a: => A): State[S, A] = State(s => (a, s))

    override def flatMap[A, B](fa: State[S, A])(f: A => State[S, B]): State[S, B] = State { s1 =>
      fa.run(s1) match
        case (a, s2) => f(a).run(s2)
    }
end state
