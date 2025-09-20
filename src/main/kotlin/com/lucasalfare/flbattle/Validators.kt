package com.lucasalfare.flbattle

/**
 * Interface base para todos os validadores de combate.
 *
 * Cada validador verifica uma condição específica antes que uma ação seja aplicada
 * (como dano, buffs, habilidades). Retorna true se a ação é válida, false caso contrário.
 */
interface Validator {
  /**
   * Valida se a ação é permitida.
   *
   * @param attacker Fighter que realiza a ação
   * @param defender Fighter que recebe a ação
   * @param damage Valor de dano calculado antes da validação
   * @return true se a ação é válida, false se deve ser bloqueada
   */
  fun validate(attacker: Fighter, defender: Fighter, damage: Int): Boolean
}

/**
 * Função auxiliar para aplicar dano com validação.
 *
 * @param attacker Fighter que realiza o ataque
 * @param defender Fighter que recebe o ataque
 * @param damage Valor de dano calculado previamente
 * @param validators Lista de validadores ativos neste combate ou turno
 */
fun applyDamageWithValidation(
  attacker: Fighter,
  defender: Fighter,
  damage: Int,
  validators: List<Validator>
) {
  if (validators.all { it.validate(attacker, defender, damage) }) {
    defender.receiveDamage(damage, attacker)
  } else {
    println("Ação bloqueada por validação (ataque de ${attacker.name} a ${defender.name})")
  }
}