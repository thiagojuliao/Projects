import exercises.part1.chapter6.State
import exercises.part1.chapter6.State.*

import scala.annotation.tailrec

val inc    = State[Int, Int](n => (n, n + 1))
val double = State[Int, Int](n => (n, n * 2))

/** 6.10 */
inc.map(_ * 2).run(42)
inc.flatMap(n => State(m => (n + m, m * 3))).run(42)
inc.map2(double)(_ + _).run(42)

State.sequence(List(inc, double, inc)).run(42)

/** 6.11: Hard: To get some experience using State, implement a finite state automaton that models a simple candy dispenser. The machine has two types
  * of input: you can insert a coin, or you can turn the knob to dispense candy. It can be in one of two states: locked or unlocked. It also tracks
  * how many candies are left and how many coins it contains:
  */

/** The rules of the machine are as follows: */
/**  Inserting a coin into a locked machine will cause it to unlock if there’s any candy left. */
/**  Turning the knob on an unlocked machine will cause it to dispense candy and become locked. */
/**  Turning the knob on a locked machine or inserting a coin into an unlocked machine does nothing. */
/**  A machine that’s out of candy ignores all inputs. */

enum Input:
  case Coin, Turn

case class Machine(locked: Boolean, candies: Int, coins: Int) {

  def insert(input: Input): Machine = input match
    case _ if candies <= 0     => this
    case Input.Coin if locked  => copy(locked = false, coins = coins + 1)
    case Input.Coin if !locked => this
    case Input.Turn if !locked => copy(locked = true, candies = candies - 1)
    case Input.Turn if locked  => this
    case _                     => this

  /** Solution from the Red Book */
  def update(i: Input, s: Machine): Machine =
    (i, s) match
      case (_, Machine(_, 0, _))                     => s
      case (Input.Coin, Machine(false, _, _))        => s
      case (Input.Turn, Machine(true, _, _))         => s
      case (Input.Coin, Machine(true, candy, coin))  => Machine(false, candy, coin + 1)
      case (Input.Turn, Machine(false, candy, coin)) => Machine(true, candy - 1, coin)
}

/** The simulateMachine method should operate the machine based on the list of inputs and return the number of coins and candies left in the machine
  * at the end. For example, if the input Machine has 10 coins and five candies, and a total of four candies are successfully bought, the output
  * should be (14, 1):
  */
def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
  State { sm =>
    @tailrec
    def simulate(inputs: List[Input], sm: Machine): ((Int, Int), Machine) =
      inputs match
        case Nil             => (sm.coins, sm.candies) -> sm
        case input :: inputs =>
          simulate(inputs, sm.insert(input))

    simulate(inputs, sm)
  }

/** Solution from the Read Book (much more concise) */
def simulateMachineR(inputs: List[Input]): State[Machine, (Int, Int)] =
  for
    _ <- State.traverse(inputs)(i => State.modify[Machine](sm => sm.update(i, sm)))
    s <- State.get
  yield (s.coins, s.candies)

val initialMachineState = Machine(true, 5, 10)
val inputs              = List(Input.Coin, Input.Turn, Input.Coin, Input.Turn, Input.Coin, Input.Turn, Input.Coin, Input.Turn)

simulateMachine(inputs).run(initialMachineState)._1
simulateMachineR(inputs).run(initialMachineState)._1
