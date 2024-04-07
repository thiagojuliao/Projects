package year2015.day21

sealed trait GameCharacter:
  def hp: Int
  def damage: Int
  def armor: Int

  def isAlive: Boolean = hp > 0

  def isDead: Boolean = !isAlive

  def takeDamage(amount: Int): GameCharacter
end GameCharacter

case class Boss(hp: Int, damage: Int, armor: Int) extends GameCharacter:
  override def takeDamage(amount: Int): Boss = copy(hp = hp - amount)

object Boss:
  def make(attributes: List[String]): Boss =
    val hp :: dmg :: armor :: _ = attributes.map(_.split(": ").last.toInt): @unchecked
    Boss(hp, dmg, armor)
end Boss

case class Player(hp: Int, damage: Int, armor: Int, equipments: List[Equipment] = List()) extends GameCharacter:
  override def takeDamage(amount: Int): Player = copy(hp = hp - amount)

  def equip(equipment: Equipment): Player =
    copy(damage = damage + equipment.damage, armor = armor + equipment.armor, equipments = equipment :: equipments)

  def equipAll(eqs: List[Equipment]): Player =
    eqs.foldLeft(this)((player, equipment) => player.equip(equipment))

object Player:
  def make: Player = Player(100, 0, 0)
end Player
