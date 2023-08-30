package year2015.day9

import utils.Ops

object Solution extends App with Ops:
  private val input  = loadMultilineInput(2015, 9)
  private val sample =
    List("London to Dublin = 464", "London to Belfast = 518", "Dublin to Belfast = 141")

  type Path = List[String]

  type Graph = Map[(String, String), Int]

  object Graph:
    private val path = "(\\w+) to (\\w+) = (\\d+)".r

    def parse(s: String): Graph = s match
      case path(start, end, d) =>
        Map((start, end) -> d.toInt) ++ Map((end, start) -> d.toInt)

      case _ => sys.error(s"invalid path string: $s")

    def buildFrom(ls: List[String]): Graph =
      ls.map(Graph.parse).reduce(_ ++ _)

  def getPaths(g: Graph): List[Path] =
    g.keys.flatMap(t => t._1 :: t._2 :: Nil).toList.permutations.toList

  def getPathValue(p: Path, g: Graph): Int =
    p.zip(p.tail).map(g(_)).sum

  def part1(input: List[String]): Int =
    val graph = Graph.buildFrom(input)
    val paths = getPaths(graph)
    paths.map(getPathValue(_, graph)).min

  def part2(input: List[String]): Int =
    val graph = Graph.buildFrom(input)
    val paths = getPaths(graph)
    paths.map(getPathValue(_, graph)).max

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
