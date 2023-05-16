package br.com.ifood.sdqv.core

import br.com.ifood.sdqv.core.GeneralTypes.Validations

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.util.matching.Regex
import scala.util.{Failure, Try}
import scala.collection.mutable.ListBuffer

case object GenericValidationFunctions {

  /**
   * Data Quality Validation - Is Never Null
   *
   * Checks if a column value is not null, if not the dirty record counter will be incremented
   * @param dataframe The input dataframe to be validated
   * @param validations A map containing all validations to be made
   * @return A new dataframe with the dirty columns updated and ready to be count
   */
  def isNeverNull(dataframe: DataFrame, validations: Validations): DataFrame = {
    def applyValidation(dataframe: DataFrame, column: String): DataFrame = {
      val dirtyColumn: String = s"isNeverNull->$column"

      val updatedDirtyColumnsExpression: Column = expr(
        s"""
          | case
          |   when $column is not null then dirty_columns
          |   else array_union(dirty_columns, array("$dirtyColumn"))
          | end
          |""".stripMargin
      )

      dataframe.withColumn("dirty_columns", updatedDirtyColumnsExpression)
    }

    if (validations.contains("isNeverNull")) {
      val columns: List[String] = validations("isNeverNull").asInstanceOf[List[String]]

      columns.foldLeft(dataframe)(applyValidation)
    } else dataframe
  }

  /**
   * Data Quality Validation - Is Always Null
   *
   * Checks if a column value is null, if not the dirty record counter will be incremented
   * @param dataframe The input dataframe to be validated
   * @param validations A map containing all validations to be made
   * @return A new dataframe with the dirty columns updated and ready to be count
   */
  def isAlwaysNull(dataframe: DataFrame, validations: Validations): DataFrame = {
    def applyValidation(dataframe: DataFrame, column: String): DataFrame = {
      val dirtyColumn: String = s"isAlwaysNull->$column"

      val updatedDirtyColumnsExpression: Column = expr(
        s"""
           | case
           |   when $column is null then dirty_columns
           |   else array_union(dirty_columns, array("$dirtyColumn"))
           | end
           |""".stripMargin
      )

      dataframe.withColumn("dirty_columns", updatedDirtyColumnsExpression)
    }

    if (validations.contains("isAlwaysNull")) {
      val columns: List[String] = validations("isAlwaysNull").asInstanceOf[List[String]]

      columns.foldLeft(dataframe)(applyValidation)
    } else dataframe
  }

  /**
   * Data Quality Validation - Is Any Of
   *
   * Checks if a column value is within a specific domain, if not the dirty record counter will be incremented
   * @param dataframe The input dataframe to be validated
   * @param validations A map containing all validations to be made
   * @return A new dataframe with the dirty columns updated and ready to be count
   */
  def isAnyOf(dataframe: DataFrame, validations: Validations): DataFrame = {
    def applyValidation(dataframe: DataFrame, column: String, domain: List[Any]): DataFrame = {
      val dirtyColumn: String = s"isAnyOf->$column"

      val updatedDirtyColumnsExpression: Column = expr(
        s"""
           | case
           |  when flag then dirty_columns
           |  else array_union(dirty_columns, array("$dirtyColumn"))
           | end
           |""".stripMargin
      )

      dataframe.withColumn("flag", col(column).isin(domain:_*))
        .withColumn("dirty_columns", updatedDirtyColumnsExpression)
        .drop("flag")
    }

    if (validations.contains("isAnyOf")) {
      val columnsAndDomains: Map[String, List[Any]] = validations("isAnyOf").asInstanceOf[Map[String, List[Any]]]

      columnsAndDomains.foldLeft(dataframe) { (dataframe, columnAndDomain) =>
        applyValidation(dataframe, columnAndDomain._1, columnAndDomain._2)
      }
    } else dataframe
  }

  /**
   * Data Quality Validation - Is Formatted As Date [UDF]
   *
   * Checks if a column value can be formatted as a date by a specific format, if not the dirty record counter will be incremented
   * @param validations A map containing all validations to be made
   * @param values List of column values to be checked
   * @return List of columns that did not pass the validation
   */
  def isFormattedAsDate(validations: Validations)(values: Any*): List[String] = {
    val result: ListBuffer[String] = ListBuffer()
    val maybeValues : List[Option[Any]] = values.map(Option(_)).toList

    if (validations.contains("isFormattedAsDate")) {
      val isFormattedAsDateValidations: Map[String, String] = validations("isFormattedAsDate").asInstanceOf[Map[String, String]]
      val datesAndValues: List[(String, Option[Any])] = isFormattedAsDateValidations.keys.zip(maybeValues).toList

      datesAndValues.foreach { dateAndValue =>
        val column: String = dateAndValue._1
        val value: Option[Any] = dateAndValue._2

        val datePattern: String = isFormattedAsDateValidations(column)
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

        if (value.isDefined) {
          Try(LocalDate.parse(value.get.toString, dateFormatter)) match {
            case Failure(_) =>
              result += s"isFormattedAsDate->$column"
            case _ =>
          }
        }
      }
    }

    result.toList
  }

  /**
   * Data Quality Validation - Is Matching Regex [UDF]
   *
   * Checks if a column value can be matched against a regular expression, if not the dirty record counter will be incremented
   * @param validations A map containing all validations to be made
   * @param values List of column values to be checked
   * @return List of columns that did not pass the validation
   */
  def isMatchingRegex(validations: Validations)(values: Any*): List[String] = {
    val result: ListBuffer[String] = ListBuffer()
    val maybeValues : List[Option[Any]] = values.map(Option(_)).toList

    if (validations.contains("isMatchingRegex")) {
      val isMatchingRegexValidations: Map[String, String] = validations("isMatchingRegex").asInstanceOf[Map[String, String]]
      val columnsAndValues: List[(String, Option[Any])] = isMatchingRegexValidations.keys.zip(maybeValues).toList

      columnsAndValues.foreach { columnAndValue =>
        val column: String = columnAndValue._1
        val value: Option[Any] = columnAndValue._2

        val pattern: Regex = isMatchingRegexValidations(column).r

        if (value.isDefined) {
          value.get.toString match {
            case pattern(_*) =>
            case _ =>
              result += s"isMatchingRegex->$column"
          }
        }
      }
    }

    result.toList
  }

  /**
   * Data Quality Validation - Satisfies
   *
   * Checks if the passed condition is satisfied, if not the dirty record counter will be incremented
   * @param dataframe Input dataframe to be validated (must have already been validated by the other conditions)
   * @param validations A map containing all validations to be made
   * @return A new dataframe with the dirty columns updated and ready to be count
   */
  def satisfies(dataframe: DataFrame, validations: Validations): DataFrame = {
    val impliesExpressionPattern: Regex = "(.*)?([<>=!]{1,2}.*)->(.*)".r

    def formatCondition(condition: String): String = {
      condition match {
        case impliesExpressionPattern(c1, cond1, cond2) =>
          s"not (coalesce($c1, '') $cond1) or $cond2"
        case expression => expression
      }
    }

    def applyValidation(dataframe: DataFrame, condition: String): DataFrame = {
      val dirtyColumn: String = s"satisfies->${condition.replace("->", "=>")}"
      val formattedCondition: String = formatCondition(condition)

      val updatedDirtyColumnsExpression: Column = expr(
        s"""
          | case
          |   when $formattedCondition then dirty_columns
          |   else array_union(dirty_columns, array("$dirtyColumn"))
          | end
          |""".stripMargin)

      dataframe.withColumn("dirty_columns", updatedDirtyColumnsExpression)
    }

    if (validations.contains("satisfies")) {
      val conditions: List[String] = validations("satisfies").asInstanceOf[List[String]]

      conditions.foldLeft(dataframe)(applyValidation)
    } else dataframe
  }
}
