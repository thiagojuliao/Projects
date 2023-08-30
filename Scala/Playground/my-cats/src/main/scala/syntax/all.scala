package syntax

object all extends ApplicativeSyntax with MonadSyntax {
  extension [A](a: A) def show: Unit = println(a)
}
