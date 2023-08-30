package playgrounds

import syntax.all.*
import instances.all.given

object ApplicativePlayground extends App {
  case class Point2D(x: Int, y: Int)
  case class Point3D(x: Int, y: Int, z: Int)

  val ox = Option(1)
  val oy = Option(2)
  val oz = Option(3)

  (ox, oy).map2(Point2D.apply).show
  (ox, oy, oz).map3(Point3D.apply).show
}
