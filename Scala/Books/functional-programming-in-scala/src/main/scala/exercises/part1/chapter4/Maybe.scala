package exercises.part1.chapter4

enum Maybe[+A]:
  case None
  case Just(get: A)

  /** 4.1: Implement all of the preceding functions on Option. As you implement each function, try to think about what it means and in what situations
    * you’d use it. We’ll explore when to use each of these functions next.
    */
  def map[B](f: A => B): Maybe[B] = this match
    case None    => None
    case Just(a) => Just(f(a))

  def flatMap[B](f: A => Maybe[B]): Maybe[B] =
    this.map(f).getOrElse(None)

  def getOrElse[B >: A](default: => B): B = this match
    case None    => default
    case Just(a) => a

  def orElse[B >: A](ob: => Maybe[B]): Maybe[B] =
    ob.map(b => this.getOrElse(b))

  def filter(p: A => Boolean): Maybe[A] =
    this.flatMap(a => if p(a) then Maybe(a) else None)

object Maybe:
  def apply[A](a: A): Maybe[A] = Just(a)
