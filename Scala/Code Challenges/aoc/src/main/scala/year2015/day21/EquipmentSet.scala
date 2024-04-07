package year2015.day21

export EquipmentSet.*

object EquipmentSet:
  def equipmentCombinations: Seq[List[Equipment]] =
    for
      weapon <- ItemShop.weapons
      armor  <- ItemShop.armors
      ring1  <- ItemShop.rings
      ring2  <- ItemShop.rings if ring1 != ring2 || ring1 == emptyRing
    yield List(weapon, armor, ring1, ring2)

  extension (self: List[Equipment]) def totalCost: Int = self.map(_.cost).sum
