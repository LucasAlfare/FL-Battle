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