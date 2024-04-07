package year2015.day21

export Weapon.*
export Armor.*
export Ring.*

sealed trait Equipment:
  def name: String
  def cost: Int
  def damage: Int
  def armor: Int
end Equipment

case class Weapon(name: String, cost: Int, damage: Int) extends Equipment:
  override val armor: Int = 0

object Weapon:
  val dagger: Equipment     = Weapon("Dagger", 8, 4)
  val shortsword: Equipment = Weapon("Shortsword", 10, 5)
  val warhammer: Equipment  = Weapon("Warhammer", 25, 6)
  val longsword: Equipment  = Weapon("Longsword", 40, 7)
  val greataxe: Equipment   = Weapon("Greataxe", 74, 8)
end Weapon

case class Armor(name: String, cost: Int, armor: Int) extends Equipment:
  override val damage: Int = 0

object Armor:
  val leather: Equipment    = Armor("Leather", 13, 1)
  val chainmail: Equipment  = Armor("Chainmail", 31, 2)
  val splintmail: Equipment = Armor("Splintmail", 53, 3)
  val bandedmail: Equipment = Armor("Bandedmail", 75, 4)
  val platemail: Equipment  = Armor("Platemail", 102, 5)
  val emptyArmor: Equipment = Armor("Empty", 0, 0)
end Armor

case class Ring(name: String, cost: Int, damage: Int, armor: Int) extends Equipment

object Ring:
  val dmgring1: Equipment = Ring("Damage +1", 25, 1, 0)
  val dmgring2: Equipment = Ring("Damage +2", 50, 2, 0)
  val dmgring3: Equipment = Ring("Damage +3", 100, 3, 0)

  val defring1: Equipment = Ring("Defense +1", 20, 0, 1)
  val defring2: Equipment = Ring("Defense +2", 40, 0, 2)
  val defring3: Equipment = Ring("Defense +3", 80, 0, 3)

  val emptyRing: Equipment = Ring("Empty", 0, 0, 0)
end Ring
