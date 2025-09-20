package com.lucasalfare.flbattle

/**
 * Interface que define uma regra de cálculo de dano ou efeito.
 *
 * Cada implementação de Rule é independente e modular.
 */
interface Rule {

  /**
   * Calcula o dano ou efeito que um atacante causará a um defensor.
   *
   * @param attacker Fighter que está atacando
   * @param defender Fighter que está recebendo o ataque
   * @return valor inteiro de dano calculado
   */
  fun calculate(attacker: Fighter, defender: Fighter): Int
}

/** Regra de ataque físico simples: dano = strength - defesa */
class PhysicalAttackRule : Rule {
  override fun calculate(attacker: Fighter, defender: Fighter): Int {
    val raw = attacker.attributes.get("strength")
    val reduced = raw - defender.attributes.get("defense")
    return reduced.coerceAtLeast(1)
  }
}

/** Regra de ataque mágico simples: dano = intelligence - magic_resist */
class MagicAttackRule : Rule {
  override fun calculate(attacker: Fighter, defender: Fighter): Int {
    val raw = attacker.attributes.get("intelligence")
    val reduced = raw - defender.attributes.get("magic_resist")
    return reduced.coerceAtLeast(1)
  }
}