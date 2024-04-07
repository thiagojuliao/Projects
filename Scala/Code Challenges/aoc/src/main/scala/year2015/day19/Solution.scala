package year2015.day19

import utils.Ops

import scala.annotation.tailrec
import scala.util.Random

object Solution extends App with Ops:
  private val input   = loadMultilineInput(2015, 19)
  private val sample1 = loadMultilineInput(2015, 19, "sample1.txt")
  private val sample2 = loadMultilineInput(2015, 19, "sample2.txt")

  def parseInput(s: String): (String, String) =
    val pattern = "(\\w+) => (\\w+)".r
    s match
      case pattern(mol, rep) => mol -> rep
      case _                 => sys.error(s"invalid pattern @ $s")

  def getReplacementsMap(input: List[(String, String)]): Map[String, List[String]] =
    input.groupBy(_._1).view.mapValues(_.map(_._2)).toMap

  def getDistinctMoleculesFrom(molecule: String, medicine: String, replacements: List[String]): List[String] =
    @tailrec
    def loop(index: Int, result: Set[String]): List[String] =
      val nextMoleculeAt = medicine.indexOf(molecule, index)

      if index == medicine.length || nextMoleculeAt == -1 then result.toList
      else
        val first     = medicine.slice(0, nextMoleculeAt)
        val second    = medicine.substring(nextMoleculeAt + molecule.length)
        val molecules = replacements.map(first + _ + second)

        loop(index + 1, result ++ molecules)
    loop(0, Set())

  /** Solution based on the one given by zhiayang at: https://github.com/zhiayang/adventofcode/blob/master/2015/day19/part2.py
    */
  def calculateMinimumNumberOfSteps(target: String, transformations: List[(String, String)]): Int =
    def replaceAndCount(word: String, replace: String, result: String): (String, Int) =
      val regex     = word.r
      val newCount  = regex.findAllIn(result).length
      val newResult = regex.replaceAllIn(result, replace)
      newResult -> newCount

    @tailrec
    def transformAll(transformations: List[(String, String)], string: String, count: Int = 0): (String, Int) =
      if transformations.isEmpty then (string, count)
      else
        val (from, to)            = transformations.head
        val (newString, newCount) = replaceAndCount(to, from, string)
        transformAll(transformations.tail, newString, count + newCount)

    @tailrec
    def calculate(molecule: String, transformations: List[(String, String)], count: Int = 0): Int =
      if molecule.length <= 1 then count
      else
        val (result, newCount) = transformAll(transformations, molecule)
        val shuffled           = Random.shuffle(transformations)

        if result == molecule then calculate(target, shuffled)
        else calculate(result, shuffled, count + newCount)

    calculate(target, transformations)

  def part1(input: List[String]): Int =
    val parsedInput = input.init.map(parseInput)
    val medicine    = input.last
    val repMap      = getReplacementsMap(parsedInput)

    repMap.flatMap { case (molecule, replacements) => getDistinctMoleculesFrom(molecule, medicine, replacements) }.toSet.size

  def part2(input: List[String]): Int =
    val parsedInput = input.init.map(parseInput)
    val medicine    = input.last

    calculateMinimumNumberOfSteps(medicine, parsedInput)

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
