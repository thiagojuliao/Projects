package year2016.day8

import Instructions.Instruction

final case class Screen private (width: Int, height: Int, pixels: Seq[Pixel]):
  def setPixels(pixels: Seq[Pixel]): Screen =
    this.copy(pixels = pixels)

  def updateWith(instructions: List[Instruction]): Screen =
    instructions.foldLeft(this)((state, inst) => inst(state))

  override def toString: String =
    pixels.sorted.grouped(width).map(_.mkString).mkString("\n")

object Screen:
  def make(width: Int = 50, height: Int = 6): Screen =
    val initState = for x <- 0 until height; y <- 0 until width yield Pixel(x, y)
    Screen(width, height, initState)
