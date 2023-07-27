package validators

trait BusinessRuleValidator {
  type ValidationResult
  private type BusinessRule[T] = T => ValidationResult

  def makeRule[T](f: T => ValidationResult): BusinessRule[T] = f

  def validateAll: ValidationResult
}
