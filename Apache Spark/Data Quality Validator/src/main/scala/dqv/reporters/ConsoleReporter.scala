package br.com.ttj
package dqv.reporters
import dqv.validator.{DataQualityValidation, DataQualityValidator}

import dqv.validations.Observation

import java.text.SimpleDateFormat

object ConsoleReporter extends DataQualityReporter {
  import Console._

  private def printHeader(displayName: String, totalRecords: Long): Unit = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val timestamp: String = sdf.format(new java.util.Date())

    val header: String =
      s"""
         |$BLUE##### 📑 Validation Report - $displayName [$timestamp] #####
         |$BLUE🛡️ A total of $totalRecords records were validated against the following set of rules:$RESET
         |""".stripMargin

    println(header)
  }

  private def printValidationResult(validation: DataQualityValidation)(implicit observer: Observer): Unit = {
    val color: String = {
      if (validation.check) GREEN
      else RED
    }

    val result: String = s"$color${validation.report}$RESET"

    println(result)
  }

  private def printObservationResult(observation: Observation)(implicit observer: Observer): Unit = {
    val result: String = s"$CYAN${observation.report}$RESET"

    println(result)
  }

  private def printMetricsOverhaulResult(totalDirtyRecords: Long): Unit = {
    val footer: String = {
      if (totalDirtyRecords > 0)
        s"$YELLOW⚠️ Not all validations were satisfied. 😔$RESET"
      else
        s"${GREEN}Nicely done! All validations passed! 😎$RESET"
    }

    val result: String =
      s"""
         |$BLUE##### 📊 Metrics Overhaul Result - $totalDirtyRecords total dirty records found! #####$RESET
         |$footer
         |""".stripMargin

    println(result)
  }

  override def report(validator: DataQualityValidator): Unit = {
    val displayName: String = validator.getDisplayName
    val validations: Seq[DataQualityValidation] = validator.getValidations
    val observations: Seq[Observation] = validator.getObservations
    val totalRecords: Long = validator.getValidationResult("total_records")
    val totalDirtyRecords: Long = validator.getValidationResult("total_dirty_records")

    implicit val observer: Observer = validator.getObserver

    printHeader(displayName, totalRecords)

    validations.foreach(printValidationResult)

    observations.foreach(printObservationResult)

    printMetricsOverhaulResult(totalDirtyRecords)
  }
}
