package core

import algebra.Validated

object common {
  type Valid[A] = Validated[List[String], A]
}
