package exercises.part3.chapter11

case class Id[A](value: A):
  /** 11.17: Implement map and flatMap as methods on this class, and give an implementation for Monad[Id].
    */
  def map[B](f: A => B): Id[B] =
    Id(f(value))

  def flatMap[B](f: A => Id[B]): Id[B] =
    f(value)
