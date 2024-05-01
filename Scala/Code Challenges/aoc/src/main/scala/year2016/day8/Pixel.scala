package year2016.day8

final case class Pixel(x: Int, y: Int, on: Boolean = false):
  def turnOn: Pixel  = this.copy(on = true)
  def turnOff: Pixel = this.copy(on = false)

  override def toString: String =
    if on then "#" else "."

object Pixel:
  given Ordering[Pixel] = (p1: Pixel, p2: Pixel) =>
    if p1.x == p2.x then p1.y.compare(p2.y)
    else p1.x.compare(p2.x)
