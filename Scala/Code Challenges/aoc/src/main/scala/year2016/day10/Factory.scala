package year2016.day10

import scala.annotation.tailrec

final case class Factory private (bots: Map[Int, Bot], output: Map[Int, Int]):
  def sendValueTo(value: Int, botId: Int): Factory =
    if !bots.contains(botId) then this.copy(bots = bots + (botId -> Bot(botId).receive(value)))
    else this.copy(bots = bots.updated(botId, bots(botId).receive(value)))

  def transferLow(b1: Bot, b2: Bot): Factory =
    val (b1_, b2_) = b1.transferLowTo(b2)
    this.copy(bots = bots + (b1.id -> b1_) + (b2.id -> b2_))

  def transferLow(b: Bot, out: Int): Factory =
    val b_ = b.copy(low = None)
    this.copy(bots = bots + (b.id -> b_), output = output + (out -> b.low.get))

  def transferHigh(b1: Bot, b2: Bot): Factory =
    val (b1_, b2_) = b1.transferHighTo(b2)
    this.copy(bots = bots + (b1.id -> b1_) + (b2.id -> b2_))

  def transferHigh(b: Bot, out: Int): Factory =
    val b_ = b.copy(high = None)
    this.copy(bots = bots + (b.id -> b_), output = output + (out -> b.high.get))

  def process(instruction: String): (Boolean, Factory) =
    instruction match
      case s"value $v goes to bot $b" =>
        true -> sendValueTo(v.toInt, b.toInt)

      case s"bot $b1 gives low to bot $b2 and high to bot $b3" =>
        val id1 = b1.toInt; val id2 = b2.toInt; val id3 = b3.toInt

        if bots.contains(id1) && bots(id1).isReady then
          val bot1 = bots(id1)
          val bot2 = bots.getOrElse(id2, Bot(id2))
          val bot3 = bots.getOrElse(id3, Bot(id3))

          true -> this.transferLow(bot1, bot2).transferHigh(bot1, bot3)
        else false -> this

      case s"bot $b1 gives low to output $o1 and high to bot $b2" =>
        val id1 = b1.toInt; val id2 = o1.toInt; val id3 = b2.toInt

        if bots.contains(id1) && bots(id1).isReady then
          val bot1 = bots(id1)
          val bot2 = bots.getOrElse(id3, Bot(id3))
          true -> this.transferLow(bot1, id2).transferHigh(bot1, bot2)
        else false -> this

      case s"bot $b1 gives low to output $o1 and high to output $o2" =>
        val id1 = b1.toInt; val id2 = o1.toInt; val id3 = o2.toInt

        if bots.contains(id1) && bots(id1).isReady then
          val bot = bots(id1)
          true -> this.transferLow(bot, id2).transferHigh(bot, id3)
        else false -> this

      case _ => sys.error(s"Invalid instruction: $instruction")

  def processAll(instructions: List[String]): Factory =
    @tailrec
    def loop(instr: List[String], state: Factory): Factory =
      if instr.isEmpty then state
      else
        val (processed, state_) = state.process(instr.head)
        if processed then loop(instr.tail, state_) else loop(instr.tail :+ instr.head, state)
    loop(instructions, this)
end Factory

object Factory:
  def create(): Factory = Factory(Map(), Map())
