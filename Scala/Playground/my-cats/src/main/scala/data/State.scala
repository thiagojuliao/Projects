package data

case class State[S, A](st: S => (S, A)) {
  def run(s: S): (S, A) = st(s)

  def runS(s: S): S = run(s)._1

  def runA(s: S): A = run(s)._2

  def map[B](f: A => B): State[S, B] =
    State { st_ =>
      {
        val (s_, a) = run(st_)
        (s_, f(a))
      }
    }

  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State { st_ =>
      {
        val (s_, a) = run(st_)
        f(a).run(s_)
      }
    }
}

object State {
  def pure[S, A](a: A): State[S, A] = State(s => (s, a))
}
