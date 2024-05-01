package year2016.day8

object Instructions:
  type Instruction = Screen => Screen

  def parse(s: String): Instruction = screen =>
    s match
      case s"rect ${a}x$b" =>
        val updatedPixels = screen.pixels.map { case p @ Pixel(x, y, _) =>
          if (0 until b.toInt).contains(x) && (0 until a.toInt).contains(y) then p.turnOn else p
        }
        screen.setPixels(updatedPixels)

      case s"rotate column x=$a by $n" =>
        val updatedPixels = screen.pixels.map { case p @ Pixel(x, y, s) =>
          if y == a.toInt then Pixel((x + n.toInt) % screen.height, y, s)
          else p
        }
        screen.setPixels(updatedPixels)

      case s"rotate row y=$b by $n" =>
        val updatedPixels = screen.pixels.map { case p @ Pixel(x, y, s) =>
          if x == b.toInt then Pixel(x, (y + n.toInt) % screen.width, s)
          else p
        }
        screen.setPixels(updatedPixels)
