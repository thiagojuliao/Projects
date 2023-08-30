package exercises.part3.chapter10

object instances:
  /** 10.1: Give Monoid instances for integer addition and multiplication as well as the Boolean operators
    */
  object int:
    given Monoid[Int] = new:
      val empty: Int = 0

      def combine(x: Int, y: Int): Int = x + y

    val intMultiplication: Monoid[Int] = new:
      val empty: Int = 1

      def combine(x: Int, y: Int): Int = x * y

  object boolean:
    val booleanOr: Monoid[Boolean] = new:
      val empty: Boolean = false

      def combine(b1: Boolean, b2: Boolean): Boolean = b1 || b2

    val booleanAnd: Monoid[Boolean] = new:
      val empty: Boolean = true

      def combine(b1: Boolean, b2: Boolean): Boolean = b1 && b2

  object option:
    /** 10.2: Give a Monoid instance for combining Option values */
    def optionMonoid[A](using M: Monoid[A]): Monoid[Option[A]] = new:
      val empty: Option[A] = None

      def combine(o1: Option[A], o2: Option[A]): Option[A] =
        o1 -> o2 match
          case None -> None         => None
          case None -> Some(a)      => Some(a)
          case Some(a) -> None      => Some(a)
          case Some(a1) -> Some(a2) => Some(M.combine(a1, a2))

  /** 10.3: A function having the same argument and return type is sometimes called an endofunction.2 Write a monoid for endofunctions
    */
  object functions:
    def endoMonoid[A]: Monoid[A => A] = new:
      val empty: A => A = identity

      def combine(f: A => A, g: A => A): A => A =
        f compose g

end instances
