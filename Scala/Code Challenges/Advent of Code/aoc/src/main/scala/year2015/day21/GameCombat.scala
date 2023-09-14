package year2015.day21

export GameCombat.*

object GameCombat:
  private def calculateDamage(from: GameCharacter, on: GameCharacter): Int =
    (on.armor - from.damage).abs max 1

  def duel(player: GameCharacter, enemy: GameCharacter, turn: Int): (GameCharacter, GameCharacter, Int) =
    if turn % 2 != 0 then
      val enemyDamageTaken = calculateDamage(player, enemy)
      (player, enemy.takeDamage(enemyDamageTaken), turn + 1)
    else
      val playerDamageTaken = calculateDamage(enemy, player)
      (player.takeDamage(playerDamageTaken), enemy, turn + 1)

  def canWin(player: GameCharacter, enemy: GameCharacter): Boolean =
    val (playerState, _, _) =
      Iterator
        .iterate((player, enemy, 1))(duel)
        .dropWhile((player, boss, _) => player.isAlive && boss.isAlive)
        .take(1)
        .toList
        .head

    playerState.isAlive
