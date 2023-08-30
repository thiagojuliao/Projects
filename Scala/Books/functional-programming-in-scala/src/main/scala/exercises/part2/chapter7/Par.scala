package exercises.part2.chapter7

/** Copied from GitHub repo @ https://github.com/fpinscala/fpinscala/blob/second-edition/src/main/scala/fpinscala/exercises/parallelism/Par.scala
  */
import java.util.concurrent.*

/** 7.2: Before continuing, try to come up with representations for Par that make it possible to implement the functions of our API.
  */
object Par:
  opaque type Par[A] = ExecutorService => Future[A]

  extension [A](pa: Par[A]) def run(s: ExecutorService): Future[A] = pa(s)

  // `unit` is represented as a function that returns a `UnitFuture`, which is a simple implementation of `Future` that just wraps a constant value. It doesn't use the `ExecutorService` at all. It's always done and can't be cancelled. Its `get` method simply returns the value that we gave it.
  def unit[A](a: A): Par[A] =
    es => UnitFuture(a)

  private case class UnitFuture[A](get: A) extends Future[A]:
    def isDone                                  = true
    def get(timeout: Long, units: TimeUnit): A  = get
    def isCancelled                             = false
    def cancel(evenIfRunning: Boolean): Boolean = false

  extension [A](pa: Par[A])
    // `map2` doesn't evaluate the call to `f` in a separate logical thread, in accord with our design choice of having `fork` be the sole function in the API for controlling parallelism. We can always do `fork(map2(a,b)(f))` if we want the evaluation of `f` to occur in a separate thread.
    def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      es =>
        val af = pa(es)
        val bf = pb(es)
        // This implementation of `map2` does _not_ respect timeouts. It simply passes the `ExecutorService` on to both `Par` values, waits for the results of the Futures `af` and `bf`, applies `f` to them, and wraps them in a `UnitFuture`. In order to respect timeouts, we'd need a new `Future` implementation that records the amount of time spent evaluating `af`, then subtracts that time from the available time allocated for evaluating `bf`.
        UnitFuture(f(af.get, bf.get))

  /** 7.3: Hard: Fix the implementation of map2 so it respects the contract of timeouts on Future.
    */
  extension [A](pa: Par[A])
    def map2Timeouts[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      es =>
        new Future[C]:
          private val futureA                    = pa(es)
          private val futureB                    = pb(es)
          @volatile private var cache: Option[C] = None

          def isDone = cache.isDefined
          def get()  = get(Long.MaxValue, TimeUnit.NANOSECONDS)

          def get(timeout: Long, units: TimeUnit) =
            val timeoutNanos = TimeUnit.NANOSECONDS.convert(timeout, units)
            val started      = System.nanoTime
            val a            = futureA.get(timeoutNanos, TimeUnit.NANOSECONDS)
            val elapsed      = System.nanoTime - started
            val b            = futureB.get(timeoutNanos - elapsed, TimeUnit.NANOSECONDS)
            val c            = f(a, b)
            cache = Some(c)
            c

          def isCancelled: Boolean                    = futureA.isCancelled || futureB.isCancelled
          def cancel(evenIfRunning: Boolean): Boolean =
            futureA.cancel(evenIfRunning) || futureB.cancel(evenIfRunning)

  // This is the simplest and most natural implementation of `fork`, but there are some problems with it--for one, the outer `Callable` will block waiting for the "inner" task to complete. Since this blocking occupies a thread in our thread pool, or whatever resource backs the `ExecutorService`, this implies that we're losing out on some potential parallelism. Essentially, we're using two threads when one should suffice. This is a symptom of a more serious problem with the implementation, and we will discuss this later in the chapter.
  def fork[A](a: => Par[A]): Par[A] =
    es => es.submit(new Callable[A] { def call = a(es).get })

  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  /** 7.4: This API already enables a rich set of operations. Here’s a simple example. Using lazyUnit, write a function to convert any function A => B
    * to one that evaluates its result asynchronously:
    */
  def asyncF[A, B](f: A => B): A => Par[B] =
    a => lazyUnit(f(a))

  extension [A](pa: Par[A])
    def map[B](f: A => B): Par[B] =
      pa.map2(unit(()))((a, _) => f(a))

  def sortPar(parList: Par[List[Int]]): Par[List[Int]] =
    parList.map(_.sorted)

  /** 7.5: Write this function, called sequence. No additional primitives are required; do not call run:
    */
  def sequenceSimple[A](pas: List[Par[A]]): Par[List[A]] =
    pas.foldRight(unit(List.empty[A]))((pa, acc) => pa.map2(acc)(_ :: _))

  // This implementation forks the recursive step off to a new logical thread,
  // making it effectively tail-recursive. However, we are constructing
  // a right-nested parallel program, and we can get better performance by
  // dividing the list in half, and running both halves in parallel.
  // See `sequenceBalanced` below.
  def sequenceRight[A](pas: List[Par[A]]): Par[List[A]] =
    pas match
      case Nil    => unit(Nil)
      case h :: t => h.map2(fork(sequenceRight(t)))(_ :: _)

  // We define `sequenceBalanced` using `IndexedSeq`, which provides an
  // efficient function for splitting the sequence in half.
  def sequenceBalanced[A](pas: IndexedSeq[Par[A]]): Par[IndexedSeq[A]] =
    if pas.isEmpty then unit(IndexedSeq.empty)
    else if pas.size == 1 then pas.head.map(a => IndexedSeq(a))
    else
      val (l, r) = pas.splitAt(pas.size / 2)
      sequenceBalanced(l).map2(sequenceBalanced(r))(_ ++ _)

  def sequence[A](pas: List[Par[A]]): Par[List[A]] =
    sequenceBalanced(pas.toIndexedSeq).map(_.toList)

  def parMap[A, B](ps: List[A])(f: A => B): Par[List[B]] = fork:
    val fbs: List[Par[B]] = ps.map(asyncF(f))
    sequence(fbs)

  /** 7.6: Implement parFilter, which filters elements of a list in parallel: */
  def parFilter[A](l: List[A])(f: A => Boolean): Par[List[A]] = fork:
    val pars: List[Par[List[A]]] =
      l.map(asyncF(a => if f(a) then List(a) else List()))
    sequence(pars).map(_.flatten) // convenience method on `List` for concatenating a list of lists

  /** 7.3.1 The law of mapping */
  def equal[A](e: ExecutorService)(p: Par[A], p2: Par[A]): Boolean =
    p(e).get == p2(e).get

  def delay[A](fa: => Par[A]): Par[A] =
    es => fa(es)

  /** 7.11: Implement choiceN and then choice in terms of choiceN. */
  def choice[A](cond: Par[Boolean])(t: Par[A], f: Par[A]): Par[A] =
    es =>
      if cond.run(es).get then t(es) // Notice we are blocking on the result of `cond`.
      else f(es)

  def choiceN[A](n: Par[Int])(choices: List[Par[A]]): Par[A] =
    es =>
      val ind = n.run(es).get // Full source files
      choices(ind).run(es)

  def choiceViaChoiceN[A](a: Par[Boolean])(ifTrue: Par[A], ifFalse: Par[A]): Par[A] =
    choiceN(a.map(b => if b then 0 else 1))(List(ifTrue, ifFalse))

  /** 7.12: There’s still something rather arbitrary about choiceN: the choice of List seems overly specific. Why does it matter what sort of
    * container we have? For instance, what if instead of a list of computations we have a Map of them?
    */
  def choiceMap[K, V](key: Par[K])(choices: Map[K, Par[V]]): Par[V] =
    es =>
      val k = key.run(es).get
      choices(k).run(es)

  /** 7.13: Implement this new primitive chooser, and then use it to implement choice and choiceN.
    */
  extension [A](pa: Par[A])
    def chooser[B](choices: A => Par[B]): Par[B] =
      es =>
        val k = pa.run(es).get
        choices(k).run(es)

  /* `chooser` is usually called `flatMap` or `bind`. */
  extension [A](pa: Par[A])
    def flatMap[B](choices: A => Par[B]): Par[B] =
      es =>
        val k = pa.run(es).get
        choices(k).run(es)

  def choiceViaFlatMap[A](p: Par[Boolean])(f: Par[A], t: Par[A]): Par[A] =
    flatMap(p)(b => if b then t else f)

  def choiceNViaFlatMap[A](p: Par[Int])(choices: List[Par[A]]): Par[A] =
    flatMap(p)(i => choices(i))

  /** 7.14: Implement join. Can you see how to implement flatMap using join? And can you implement join using flatMap?
    */
  // see nonblocking implementation in `Nonblocking.scala`
  def join[A](a: Par[Par[A]]): Par[A] =
    es => a.run(es).get().run(es)

  def joinViaFlatMap[A](a: Par[Par[A]]): Par[A] =
    flatMap(a)(x => x)

  extension [A](pa: Par[A])
    def flatMapViaJoin[B](f: A => Par[B]): Par[B] =
      join(pa.map(f))

object Examples:
  import Par.*

  // `IndexedSeq` is a superclass of random-access sequences like `Vector` in the standard library. Unlike lists, these sequences provide an efficient `splitAt` method for dividing them into two parts at a particular index.
  def sum(ints: IndexedSeq[Int]): Int =
    if ints.size <= 1 then
      ints.headOption.getOrElse(0) // `headOption` is a method defined on all collections in Scala. We saw this function in chapter 3.
    else
      val (l, r) = ints.splitAt(ints.size / 2) // Divide the sequence in half using the `splitAt` function.
      sum(l) + sum(r) // Recursively sum both halves and add the results together.
