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
   * @param rawDamage Valor de dano calculado antes da validação
   * @return true se a ação é válida, false se deve ser bloqueada
   */
  fun validate(attacker: Fighter, defender: Fighter, rawDamage: Int): Boolean
}