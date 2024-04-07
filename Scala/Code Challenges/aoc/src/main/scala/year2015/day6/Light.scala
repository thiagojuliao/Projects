package year2015.day6

case class Light(brightness: Int):
  def update(f: Int => Int): Light =
    val newBrightness = f(brightness)

    if newBrightness > 0 then copy(brightness = newBrightness)
    else copy(brightness = 0)

object Light:
  def make: Light = Light(0)

  enum LightState:
    case ON, OFF

    def isOn: Boolean = this match
      case ON => true
      case _  => false

    def isOff: Boolean = !isOn
    
    def turnOn: LightState = ON

    def turnOff: LightState = OFF

    def toggle: LightState = this match
      case ON  => OFF
      case OFF => ON
