package br.com.ttj
package dqv

package object validations {

  def isAlwaysNull(attr: String, threshold: Double = 1.0): AlwaysNullValidation =
    AlwaysNullValidation(attr, threshold)

  def isNeverNull(attr: String, threshold: Double = 1.0): NeverNullValidation =
    NeverNullValidation(attr, threshold)

  def isMatchingRegex(attr: String, pattern: String, threshold: Double = 1.0): MatchingRegexValidation =
    MatchingRegexValidation(attr, pattern, threshold)

  def isAnyOf(attr: String, domain: Seq[Any], threshold: Double = 1.0): AnyOfValidation =
    AnyOfValidation(attr, domain, threshold)

  def isFormattedAsDate(attr: String, pattern: String, threshold: Double = 1.0): FormattedAsDateValidation =
    FormattedAsDateValidation(attr, pattern, threshold)

  def satisfies(constraint: String, alias: String, threshold: Double = 1.0): SatisfiesValidation =
    SatisfiesValidation(constraint, alias, threshold)

  def hasUniqueKey(keys: Seq[String], threshold: Double = 1.0): UniqueKeyValidation =
    UniqueKeyValidation(keys, threshold)

  def hasNumOfRowsGreatherThan(limit: Long): NumberOfRowsGreaterThanValidation =
    NumberOfRowsGreaterThanValidation(limit)

  def hasNumOfRowsEqualTo(limit: Long): NumberOfRowsEqualToValidation =
    NumberOfRowsEqualToValidation(limit)

  def hasNumOfRowsLessThan(limit: Long): NumberOfRowsLessThanValidation =
    NumberOfRowsLessThanValidation(limit)

  def <>(constraint: String, alias: String): Observation =
    Observation(constraint, alias)
}
