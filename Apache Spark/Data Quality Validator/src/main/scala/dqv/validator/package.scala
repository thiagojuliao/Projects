package br.com.ttj
package dqv

import dqv.validations.{Validation, ValidationMetric, ValidationReport}

package object validator {

  type DataQualityValidation = Validation with ValidationMetric with ValidationReport

  object ValidationStrategies {

    sealed trait ValidationStrategy

    case object TRACK_ONLY extends ValidationStrategy

    case object DROP_DIRTY extends ValidationStrategy

    case object FAIL extends ValidationStrategy
  }
}
