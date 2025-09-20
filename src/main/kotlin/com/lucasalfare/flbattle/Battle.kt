package com.lucasalfare.flbattle

/**
 * Controla um combate simples entre dois Fighters em turnos alternados,
 * agora permitindo uso de itens durante o turno.
 *
 * @property f1 Primeiro combatente
 * @property f2 Segundo combatente
 * @property validators Lista de validadores aplicáveis a cada ação
 *
 * Alterna turnos entre os lutadores até que um deles seja derrotado.
 */
class Battle(
  private val f1: Fighter,
  private val f2: Fighter,
  private val validators: List<Validator>
) {
  /** Inicia o combate e imprime o vencedor */
  fun start() {
    var attacker = f1
    var defender = f2

    while (f1.isAlive() && f2.isAlive()) {
      println("\nTurno de ${attacker.name}")

      // Exemplo: usar itens antes de atacar (consumíveis ou buffs)
      attacker.inventory.getItems().forEach { item ->
        // Estratégia simples: usar todos os itens passivos/consumíveis
        // Em implementação futura, poderia ser escolha do player
        if (item.effects.any { it is HpRestoreEffect || it is AttributeBoostEffect || it is InvulnerabilityEffect }) {
          attacker.useItem(item, attacker)
        }
      }

      // Ataque normal
      attacker.attack(defender, validators)

      if (!defender.isAlive()) break

      // Alterna turnos
      val tmp = attacker
      attacker = defender
      defender = tmp
    }

    val winner = if (f1.isAlive()) f1.name else f2.name
    println("\nWinner: $winner")
  }
}