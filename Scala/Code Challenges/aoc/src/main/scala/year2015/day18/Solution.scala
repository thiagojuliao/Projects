package year2015.day18

import utils.Ops
import year2015.day6.*
import year2015.day6.Grid.*
import year2015.day6.Light.LightState

object Solution extends App with Ops:
  private val input  = loadMultilineInput(2015, 18)
  private val sample = loadMultilineInput(2015, 18, "sample.txt")

  private val lightStateParser: Char => LightState =
    c => if c == '#' then LightState.ON else LightState.OFF

  private val lightStatePrinter: LightState => String =
    lstate => if lstate == LightState.ON then "#" else "."

  private def isCorner[A](cell: Cell[A], corners: List[(Int, Int)]): Boolean =
    corners contains (cell.x, cell.y)

  def changeLightState(grid: Grid[LightState], cell: Cell[LightState]): Cell[LightState] =
    val neighbors        = grid.neighbors(cell)
    val onNeighborsCount = neighbors.count(_.value == LightState.ON)
    val light            = cell.value

    if light.isOn && (onNeighborsCount != 2 && onNeighborsCount != 3) then cell.copy(value = light.turnOff)
    else if light.isOff && onNeighborsCount == 3 then cell.copy(value = light.turnOn)
    else cell

  def changeLightStateV2(corners: List[(Int, Int)])(grid: Grid[LightState], cell: Cell[LightState]): Cell[LightState] =
    val neighbors        = grid.neighbors(cell)
    val onNeighborsCount = neighbors.count(_.value == LightState.ON)
    val light            = cell.value

    if isCorner(cell, corners) then cell
    else if light.isOn && (onNeighborsCount != 2 && onNeighborsCount != 3) then cell.copy(value = light.turnOff)
    else if light.isOff && onNeighborsCount == 3 then cell.copy(value = light.turnOn)
    else cell

  def part1(input: List[String]): Int =
    val grid = Grid.fromList(input)(lightStateParser)
    grid.modifyN(101)(changeLightState).count(_.value.isOn)

  def part2(input: List[String]): Int =
    val grid    = Grid.fromList(input)(lightStateParser)
    val corners = grid.corners

    val grid_ = grid.map { case cell @ Cell(x, y, value) =>
      if isCorner(cell, corners) then Cell(x, y, value.turnOn)
      else cell
    }

    grid_.modifyN(101)(changeLightStateV2(corners)).count(_.value.isOn)

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
