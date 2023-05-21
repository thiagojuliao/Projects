package br.com.ttj
package dqv.reporters

import dqv.validations.Observation
import dqv.validator.{DataQualityValidation, DataQualityValidator}

import java.text.SimpleDateFormat

class DatabricksHTMLReporter(htmlRenderer: String => Unit) extends DataQualityReporter {

  private val styles: String =
    """
      <style>
        p {
          font-size: 0.95rem;
        }
      </style>
    """.stripMargin

  private def buildHeader(displayName: String, totalRecords: Long): String = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val timestamp: String = sdf.format(new java.util.Date())

    val header: String =
      s"""
        <h2 style="color:#CC9933">##### 📑 Validation Report - $displayName [$timestamp] #####</h2>
        <h3 style="color:#606060">🛡️ A total of $totalRecords records were validated against the following set of rules:</h5>
        <hr>
      """.stripMargin

    header
  }

  private def buildValidationResult(validation: DataQualityValidation)(implicit observer: Observer): String = {
    val color: String = {
      if (validation.check) "#00CC00"
      else "#FF0000"
    }

    val result: String =
      s"""
        <p style="color:$color">&nbsp ${validation.report}</p>
      """.stripMargin

    result
  }

  private def buildObservationResult(observation: Observation)(implicit observer: Observer): String = {
    s"""
      <p style="color:#9900FF">&nbsp ${observation.report}</p>
    """.stripMargin
  }

  private def buildMetricsOverhaulResult(totalDirtyRecords: Long, totalDroppedRecords: Long): String = {
    val footer: String = {
      if (totalDirtyRecords > 0)
        """<h3 style="color:#606060">⚠️ Not all validations were satisfied. 😔</h3>"""
      else
        """<h3 style="color:#606060">Nicely done! All validations passed! 😎</h3>"""
    }

    s"""
      <hr>
      <h3 style="color:#CC9933">##### 📊 Metrics Overhaul Result - $totalDirtyRecords total dirty records found! #####</h3>
      <h3 style="color:#CC9933">##### 🧹 Cleaning Overhaul Result - $totalDroppedRecords records were dropped! #####</h3>
      $footer
    """.stripMargin
  }

  override def report(validator: DataQualityValidator): Unit = {
    val displayName: String = validator.getDisplayName
    val validations: Seq[DataQualityValidation] = validator.getValidations
    val observations: Seq[Observation] = validator.getObservations

    val validationResult: Map[String, Long] = validator.getValidationResult
    val totalRecords: Long = validationResult("total_records")
    val totalDirtyRecords: Long = validationResult("total_dirty_records")
    val totalDroppedRecords: Long = validationResult("total_dropped_records")

    implicit val observer: Observer = validator.getObserver

    val html: StringBuilder = new StringBuilder(s"<html>$styles<body>")

    html ++= buildHeader(displayName, totalRecords)

    validations.foreach { validation =>
      html ++= buildValidationResult(validation)
    }

    observations.foreach { observation =>
      html ++= buildObservationResult(observation)
    }

    html ++= buildMetricsOverhaulResult(totalDirtyRecords, totalDroppedRecords)

    html ++= "</body></html>"

    htmlRenderer(html.toString)
  }
}
