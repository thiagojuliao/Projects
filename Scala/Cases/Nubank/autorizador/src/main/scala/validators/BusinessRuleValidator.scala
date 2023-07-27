package validators

trait BusinessRuleValidator {
  type Env
  type ValidationResult
  private type BusinessRule[T] = T => ValidationResult

  def makeRule[T](f: T => ValidationResult): BusinessRule[T] = f

  val validateAll: Env => ValidationResult
}
