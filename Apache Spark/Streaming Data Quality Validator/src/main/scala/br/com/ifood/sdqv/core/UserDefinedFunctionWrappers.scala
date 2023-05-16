package br.com.ifood.sdqv.core

import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.expressions.UserDefinedFunction

case object UserDefinedFunctionWrappers {

  def applyValidations1(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any) => {
    val columns: List[Any] = List(v1)

    f(columns)
  })

  def applyValidations2(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any) => {
    val columns: List[Any] = List(v1, v2)

    f(columns)
  })

  def applyValidations3(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any) => {
    val columns: List[Any] = List(v1, v2, v3)

    f(columns)
  })

  def applyValidations4(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4)

    f(columns)
  })

  def applyValidations5(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any, v5: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4, v5)

    f(columns)
  })

  def applyValidations6(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any, v5: Any, v6: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4, v5, v6)

    f(columns)
  })

  def applyValidations7(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any, v5: Any, v6: Any, v7: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4, v5, v6, v7)

    f(columns)
  })

  def applyValidations8(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any, v5: Any, v6: Any, v7: Any, v8: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4, v5, v6, v7, v8)

    f(columns)
  })

  def applyValidations9(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any, v5: Any, v6: Any, v7: Any, v8: Any, v9: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4, v5, v6, v7, v8, v9)

    f(columns)
  })

  def applyValidations10(f: List[Any] => List[String]): UserDefinedFunction = udf((v1: Any, v2: Any, v3: Any, v4: Any, v5: Any, v6: Any, v7: Any, v8: Any, v9: Any, v10: Any) => {
    val columns: List[Any] = List(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)

    f(columns)
  })
}
