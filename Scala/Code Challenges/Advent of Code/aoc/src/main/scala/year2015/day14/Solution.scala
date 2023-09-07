package year2015.day14

import utils.Ops

import scala.annotation.tailrec
import scala.util.matching.Regex

object Solution extends App with Ops:
  private val input  = loadMultilineInput(2015, 14)
  private val sample = loadMultilineInput(2015, 14, "sample.txt")

  enum State:
    case Flying, Resting

  case class Reindeer(name: String, speed: Int, flysFor: Int, restsFor: Int, distanceTraveled: Int = 0, points: Int = 0):
    import State.*

    def flyFor(t: Int): Reindeer =
      @tailrec
      def fly(sec: Int, totalDistance: Int, state: State, stateDuration: Int): Int =
        if sec > t then totalDistance
        else
          state match
            case Flying  =>
              if stateDuration < flysFor then fly(sec + 1, totalDistance + speed, state, stateDuration + 1)
              else fly(sec + 1, totalDistance + speed, Resting, 1)
            case Resting =>
              if stateDuration < restsFor then fly(sec + 1, totalDistance, state, stateDuration + 1)
              else fly(sec + 1, totalDistance, Flying, 1)
      copy(distanceTraveled = fly(1, 0, Flying, 1))

    def awardPoint: Reindeer =
      copy(points = points + 1)
  end Reindeer

  object Reindeer:
    val pattern: Regex =
      "(\\w+) can fly (\\d+) km/s for (\\d+) seconds, but then must rest for (\\d+) seconds.".r

    def create(s: String): Reindeer = s match
      case pattern(name, speed, flyingDuration, restingDuration) =>
        Reindeer(name, speed.toInt, flyingDuration.toInt, restingDuration.toInt)

      case _ => sys.error(s"invalid pattern @ $s")
  end Reindeer

  object Race:
    def awardPointAt(t: Int, reindeers: List[Reindeer]): List[Reindeer] =
      val reindeers_  = reindeers.map(_.flyFor(t))
      val maxDistance = reindeers_.maxBy(_.distanceTraveled).distanceTraveled
      reindeers_.map(r => if r.distanceTraveled == maxDistance then r.awardPoint else r)

    def getWinnerByMaxDistanceTraveledAt(t: Int, reindeers: List[Reindeer]): Reindeer =
      reindeers.map(_.flyFor(t)).maxBy(_.distanceTraveled)

    def getWinnerByPointsAt(t: Int, reindeers: List[Reindeer]): Reindeer =
      List
        .range(1, t + 1)
        .foldLeft(reindeers)((rs, t) => awardPointAt(t, rs))
        .maxBy(_.points)
  end Race

  def part1(input: List[String], t: Int = 2504): Int =
    val reindeers = input.map(Reindeer.create)
    Race.getWinnerByMaxDistanceTraveledAt(t, reindeers).distanceTraveled

  def part2(input: List[String], t: Int = 2504): Int =
    val reindeers = input.map(Reindeer.create)
    Race.getWinnerByPointsAt(t, reindeers).points

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
