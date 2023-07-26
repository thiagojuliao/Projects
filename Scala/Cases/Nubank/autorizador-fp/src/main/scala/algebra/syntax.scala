package algebra

object syntax {
  extension (s: String) def err: String = s"⛔ $s"

  extension [T](value: T)
    def show(using ev: Show[T]): String = ev.show(value)

    def valid[E]: Validated[E, T] = Valid(value)

    def invalid[A]: Validated[T, A] = Invalid(value)
}
