package year2016.day5

import utils.*

import java.lang.Integer.toHexString
import java.security.MessageDigest
import scala.annotation.tailrec
import scala.util.Try

object Solution extends App with Ops:
  val input  = loadInput(2016, 5)
  val sample = loadInput(2016, 5, "sample.txt")

  private def encode(bytes: Array[Byte]): String =
    def toRight(b: Byte) = toHexString((b >>> 4) & 0x0f)

    def toLeft(b: Byte) = toHexString(b & 0x0f)

    bytes.flatMap(b => Array(toRight(b), toLeft(b))).mkString

  private def hexString(s: String): String =
    val md                  = MessageDigest.getInstance("MD5")
    val digest: Array[Byte] = md.digest(s.getBytes)
    encode(digest)

  private def getPassword(s: String): String =
    @tailrec
    def loop(index: Int, pwd: String): String =
      if pwd.length == 8 then pwd
      else
        val hs = hexString(s + index)
        if hs.startsWith("00000") then loop(index + 1, pwd + hs(5))
        else loop(index + 1, pwd)
    loop(0, "")

  private def getPassword2(s: String): String =
    @tailrec
    def loop(index: Int, pwd: Seq[String]): String =
      if pwd.mkString.length == 8 then pwd.mkString
      else
        val hs         = hexString(s + index)
        val maybeIndex = Try(hs(5).toString.toInt).toOption

        if hs.startsWith("00000") && maybeIndex.isDefined && (0 to 7 contains maybeIndex.get) && pwd(maybeIndex.get).isEmpty then
          loop(index + 1, pwd.updated(maybeIndex.get, hs(6).toString))
        else loop(index + 1, pwd)
    loop(0, Array.fill(8)(""))

  def part1(): String = getPassword(input)
  def part2(): String = getPassword2(input)

  def solve(): Unit =
    println(s"• Part 1: ${part1()}")
    println(s"• Part 2: ${part2()}")

  solve()
