package year2023.day2

case class Game(id: Int, cubeSets: Array[CubeSet]):
  def isValid: Boolean = cubeSets.forall(_.isValid)

  def minimumSet: CubeSet =
    cubeSets.foldLeft(CubeSet.default) { (acc, cs) =>
      val CubeSet(b1, r1, g1) = acc
      val CubeSet(b2, r2, g2) = cs

      CubeSet(b1 max b2, r1 max r2, g1 max g2)
    }

  def power: Int =
    val CubeSet(b, r, g) = minimumSet
    b * r * g

  override def toString: String =
    s"Game($id, ${cubeSets.mkString("[", ", ", "]")}"

object Game:
  private val pattern = "Game (\\d+): (.+)".r

  def buildFromString(string: String): Game = string match
    case pattern(id, cubes) =>
      val cubeSets = cubes.split("; ").map(CubeSet.buildFromString)
      Game(id.toInt, cubeSets)
