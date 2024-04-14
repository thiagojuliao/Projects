package year2016.day4

import utils.*
import utils.common.*

case class Room(name: String, sectorId: Int, checksum: String):
  import Room.ordering.given

  def isReal: Boolean =
    // 1 - Count the number of occurrences of each letter on the room name
    val counts = name
      .replace("-", "")
      .split("")
      .groupBy(_.head)
      .view
      .mapValues(_.length)
      .toSeq

    // 2 - Apply a specific ordering (by count and in case of a tie order it alphabetically)
    val ordered = counts.sorted

    // 3 - Now compare the first five character from ordered with the checksum and if they match the room is real
    checksum == ordered.take(5).map(_._1).mkString

  val decryptedName: String = name.map { c =>
    if c == '-' then ' '
    else alphabet((alphabet.indexOf(c) + sectorId) % 26)
  }

end Room

object Room:
  import parsers.*

  private val roomNameParser: Parser[String] =
    manyParser(conditionalParser(s => (s.head.isLetter && s.head.isLower) || s.head == '-'))

  private val checksumParser: Parser[String] =
    manyParser(alternativeParser(symbolParser, letterParser))

  def parse(s: String): Room =
    val parser = for
      roomName <- roomNameParser
      sectorId <- numberParser
      checksum <- checksumParser
    yield (roomName, sectorId, checksum)

    val (rn, si, cs) = parser.run(s)._2
    Room(rn.init, si.toInt, cs.tail.init)

  object ordering:
    given checksumOrdering: Ordering[(Char, Int)] = (x: (Char, Int), y: (Char, Int)) =>
      val countCompare     = x._2.compare(y._2)
      lazy val charCompare = x._1.compare(y._1)

      if countCompare != 0 then countCompare * -1 else charCompare

end Room
