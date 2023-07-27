package validators

trait BusinessRuleValidator[Env, R] {
  private type BusinessRule[T] = T => R

  def makeRule[T](f: T => R): BusinessRule[T] = f

  def validateAll: Env => R
}
