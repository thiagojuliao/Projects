package br.com.ttj
package dqv.metrics_store
import dqv.validator.DataQualityValidator

import org.apache.spark.internal.Logging

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat

class FileMetricsStore(
                        filePath: String,
                        format: String,
                        separator: String,
                        name: Option[String] = None
                      ) extends DataQualityMetricsStore with Logging {

  private def writeHeader(pw: PrintWriter): Unit = {
    val header: String =
      Seq("validator_id", "metric_type", "metric_name", "metric_value").reduce(_ + separator + _)

    pw.write(header + "\n")
  }

  private def writeMetrics(pw: PrintWriter, validator: DataQualityValidator): Unit = {
    val validatorId: String = validator.getName

    val metrics: Map[String, Long] =
      validator.getObserver.get.mapValues(_.asInstanceOf[Long])

    val validationResult: Map[String, Long] = validator.getValidationResult

    metrics.foreach{ case (metric, value) =>
      if (metric contains "validation.") {
        val tokens: Array[String] = metric.split("validation.")

        val metricType: String = "validation"
        val metricName: String = tokens.last.replace(".", "_")

        val row: String =
          Seq(validatorId, metricType, metricName, value.toString).reduce(_ + separator + _)

        pw.write(row + "\n")
      }
      else if (metric contains "observation.") {
        val tokens: Array[String] = metric.split("observation.")

        val metricType: String = "observation"
        val metricName: String = tokens.last.replace(".", "_")

        val row: String =
          Seq(validatorId, metricType, metricName, value.toString).reduce(_ + separator + _)

        pw.write(row + "\n")
      }
    }

    validationResult.foreach { case (metric, value) =>
      val row: String =
        Seq(validatorId, "global", metric, value.toString).reduce(_ + separator + _)

      pw.write(row + "\n")
    }
  }

  override def save(validator: DataQualityValidator): Unit = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")

    val currentDate: String = sdf.format(new java.util.Date())

    val fileName: String =
      s"$filePath${name.getOrElse(validator.getName)}_$currentDate.$format"

    val file: File = new File(fileName)
    val pw: PrintWriter = new PrintWriter(file)

    log.info(s"Writing data quality metrics to $file")

    writeHeader(pw)
    writeMetrics(pw, validator)

    pw.close()
  }
}
