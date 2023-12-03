package year2023.day2

import year2023.extensions.*

case class CubeSet(blue: Int, red: Int, green: Int):
  def isValid: Boolean =
    blue <= 14 && red <= 12 && green <= 13

object CubeSet:
  def default: CubeSet = CubeSet(0, 0, 0)
  
  def buildFromString(string: String): CubeSet =
    val map = string
      .split(", ")
      .map(s =>
        val arr = s.split(" ")
        Map(arr(1) -> arr(0).toInt)
      )
      .reduce(_ + _)

    buildFromMap(map)

  def buildFromMap(map: Map[String, Int]): CubeSet =
    val blueCount  = map.getOrElse("blue", 0)
    val redCount   = map.getOrElse("red", 0)
    val greenCount = map.getOrElse("green", 0)

    CubeSet(blueCount, redCount, greenCount)
