import exercises.part2.chapter7.Actor

import java.util.concurrent.{Callable, CountDownLatch, ExecutorService}
import java.util.concurrent.atomic.AtomicReference

/** 7.3.4 A fully non-blocking Par implementation using actors */
opaque type Future[+A] = (A => Unit) => Unit // The A => Unit function is sometimes called a continuation or a callback.
opaque type Par[+A]    = ExecutorService => Future[A]

/** Listing 7.6 Implementing run for Par */
extension [A](pa: Par[A])
  def run(es: ExecutorService): A =
    val ref   = new AtomicReference[A]
    val latch = new CountDownLatch(1)

    pa(es) { a =>
      ref.set(a); latch.countDown()
    }

    latch.await(); ref.get

object Par:

  /** It simply passes the value to the continuation.Note that the ExecutorService isn’t needed. */
  def unit[A](a: A): Par[A] =
    es => cb => cb(a) //

  /** eval forks off the evaluation of a and returns immediately. The callback will be invoked asynchronously on another thread.
    */
  def fork[A](a: => Par[A]): Par[A] =
    es => cb => eval(es)(a(es)(cb))

  /** A helper function to evaluate an action asynchronously using some ExecutorService
    */
  def eval(es: ExecutorService)(r: => Unit): Unit =
    es.submit(new Callable[Unit] {
      def call: Unit = r
    })

/** Listing 7.7 Implementing map2 with Actor */
import Par.*

extension [A](p: Par[A])
  /** this implementation is a little too liberal in forking of threads - it forks a new logical thread for the actor and for stack-safety, forks
    * evaluation of the callback `cb`
    */
  def map2[B, C](p2: Par[B])(f: (A, B) => C): Par[C] =
    es =>
      cb =>
        var ar: Option[A] = None
        var br: Option[B] = None
        val combiner      = Actor[Either[A, B]](es):
          case Left(a)  =>
            if br.isDefined then eval(es)(cb(f(a, br.get)))
            else ar = Some(a)
          case Right(b) =>
            if ar.isDefined then eval(es)(cb(f(ar.get, b)))
            else br = Some(b)
        p(es)(a => combiner ! Left(a))
        p2(es)(b => combiner ! Right(b))
