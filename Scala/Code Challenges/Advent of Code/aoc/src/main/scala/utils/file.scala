package utils

import java.io.File
import scala.io.Source

export file.*

object file:
  private def getInputFromFile(path: String): List[String] =
    val file   = new File(path)
    val source = Source.fromFile(file)
    val input  = source.getLines.toList

    source.close()
    input

  def readLine(path: String): String =
    getInputFromFile(path).head

  def readLines(path: String): List[String] =
    getInputFromFile(path)
