package year2015.day21

import utils.Ops

object Solution extends App with Ops:
  private val input  = loadMultilineInput(2015, 21)
  private val boss   = Boss.make(input)
  private val player = Player.make

  def part1: Int =
    equipmentCombinations
      .map(player.equipAll)
      .filter(canWin(_, boss))
      .map(_.equipments.totalCost)
      .min

  def part2: Int =
    equipmentCombinations
      .map(player.equipAll)
      .filterNot(canWin(_, boss))
      .map(_.equipments.totalCost)
      .max

  def solve(): Unit =
    println(s"* Part 1: $part1")
    println(s"* Part 2: $part2")

  solve()

end Solution
