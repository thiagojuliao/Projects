package year2016.day10

final case class Bot(id: Int, low: Option[Int] = None, high: Option[Int] = None, history: Seq[(Int, Int)] = Seq()):
  def isReady: Boolean = low.isDefined && high.isDefined

  def receive(n: Int): Bot =
    (low, high) match
      case None -> None    => this.copy(low = Some(n))
      case Some(l) -> None =>
        if n > l then this.copy(high = Some(n), history = history :+ (l, n))
        else this.copy(low = Some(n), high = Some(l), history = history :+ (n, l))
      case _               => sys.error("Cannot give more microchips to this bot, holding two already!")
      
  def transferLowTo(bot: Bot): (Bot, Bot) =
    this.copy(low = None) -> bot.receive(low.get)
  
  def transferHighTo(bot: Bot): (Bot, Bot) =
    this.copy(high = None) -> bot.receive(high.get)
    