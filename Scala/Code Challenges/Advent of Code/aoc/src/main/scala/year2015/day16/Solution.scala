package year2015.day16

import utils.Ops

object Solution extends App with Ops:
  private val input = loadMultilineInput(2015, 16)

  private val template = Map(
    "children"    -> 3,
    "cats"        -> 7,
    "samoyeds"    -> 2,
    "pomeranians" -> 3,
    "akitas"      -> 0,
    "vizslas"     -> 0,
    "goldfish"    -> 5,
    "trees"       -> 3,
    "cars"        -> 2,
    "perfumes"    -> 1
  )

  private type Compounds = Map[String, Int]

  def getDScore(input: Compounds, template: Compounds): Int =
    input.keys.toList
      .map(compound => (input(compound) - template(compound)).abs)
      .max

  def getD2Score(input: Compounds, template: Compounds): Int =
    input.keys.toList.map { compound =>
      if compound == "cats" || compound == "trees" then
        val diff = input(compound) - template(compound)
        if diff <= 0 then Int.MaxValue else 0
      else if compound == "pomeranians" || compound == "goldfish" then
        val diff = template(compound) - input(compound)
        if diff <= 0 then Int.MaxValue else 0
      else (input(compound) - template(compound)).abs
    }.max

  def matchScore(s: String, template: Compounds)(score: (Compounds, Compounds) => Int): (Int, Int) =
    val pattern = "Sue (\\d+): (.+)".r

    s match
      case pattern(auntN, rest) =>
        val compounds = rest.split(", ").map(_.split(": "))
        val input     = compounds.foldLeft(Map(): Compounds)((acc, arr) => acc + (arr(0) -> arr(1).toInt))

        auntN.toInt -> score(input, template)

      case _ => sys.error(s"invalid pattern @ $s")

  def part1(input: List[String]): Int =
    input
      .map(matchScore(_, template)(getDScore))
      .minBy(_._2)
      ._1

  def part2(input: List[String]): Int =
    input
      .map(matchScore(_, template)(getD2Score))
      .minBy(_._2)
      ._1

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
