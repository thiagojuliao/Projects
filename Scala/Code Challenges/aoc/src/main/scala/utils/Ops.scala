package utils

trait Ops:
  private val basePath = "src/main/scala"

  def loadInput(year: Int, day: Int, filename: String = "input.txt"): String =
    readLine(s"$basePath/year$year/day$day/$filename")

  def loadMultilineInput(year: Int, day: Int, filename: String = "input.txt"): List[String] =
    readLines(s"$basePath/year$year/day$day/$filename")
