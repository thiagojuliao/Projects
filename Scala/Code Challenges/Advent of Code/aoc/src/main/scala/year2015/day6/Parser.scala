package year2015.day6

import Light.*

trait Parser[A]:
  def parse(s: String): A

type Command[A]       = A => A
type CommandParser[A] = Parser[Command[A]]

given CommandParser[LightState] with
  def parse(s: String): Command[LightState] =
    if s == "turn on" then _.turnOn
    else if s == "turn off" then _.turnOff
    else _.toggle

given CommandParser[Light] with
  override def parse(s: String): Command[Light] =
    if s == "turn on" then _.update(_ + 1)
    else if s == "turn off" then _.update(_ - 1)
    else _.update(_ + 2)
