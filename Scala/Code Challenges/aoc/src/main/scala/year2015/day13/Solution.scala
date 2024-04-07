package year2015.day13

import utils.Ops

object Solution extends App with Ops:
  private val sample = loadMultilineInput(2015, 13, "sample.txt")
  private val input  = loadMultilineInput(2015, 13)

  type Graph = Map[String, Map[String, Int]]

  extension (self: Graph)
    def +(other: Graph): Graph =
      self.foldLeft(other) { (G, g) =>
        val (start, mp) = g
        if G contains start then G.updated(start, G(start) ++ mp) else G ++ Map(start -> mp)
      }

  object Graph:
    val pattern =
      "(\\w+) would (gain|lose) (\\d+) happiness units by sitting next to (\\w+)\\.".r

    def make(edges: List[String]): Graph =
      edges.foldLeft(Map(): Graph) { (graph, edge) =>
        edge match
          case pattern(start, sign, value, end) =>
            if sign == "gain" then graph + Map(start -> Map(end -> value.toInt))
            else graph + Map(start -> Map(end -> -value.toInt))

          case _ => sys.error(s"invalid edge pattern @ $edge")
      }

  def computeEdgeValue(start: String, end: String, g: Graph): Int =
    g(start)(end) + g(end)(start)

  def totalChangeInHappiness(from: String, g: Graph): Int =
    val paths = g.keys.toList.permutations.filter(_.head == from).map(_ :+ from)

    paths
      .map(p => p.zip(p.tail).map((start, end) => computeEdgeValue(start, end, g)))
      .map(_.sum)
      .max

  def part1(input: List[String]): Int =
    val g = Graph.make(input)
    totalChangeInHappiness("Alice", g)

  def part2(input: List[String]): Int =
    val g = Graph.make(input)

    val myEdges = g.keys.flatMap { start =>
      Map(start -> Map("Thiago" -> 0)) ::
        Map("Thiago" -> Map(start -> 0)) :: Nil
    }

    val g_ = g + myEdges.reduce(_ + _)
    totalChangeInHappiness("Alice", g_)

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
