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

// Exemplo de validadores a seguir

/**
 * Validador que garante que o HP do defensor não será reduzido abaixo de 0.
 */
class HPValidator : Validator {
  override fun validate(attacker: Fighter, defender: Fighter, damage: Int): Boolean {
    // permite dano normalmente; garante que HP final não ficará negativo
    return defender.hp - damage >= 0
  }
}

/**
 * Validador que verifica conflitos de buffs ou efeitos especiais.
 *
 * Pode ser expandido para checar buffs que anulam outros buffs,
 * status que impedem ataque, invulnerabilidades temporárias, etc.
 */
class BuffConflictValidator : Validator {
  override fun validate(attacker: Fighter, defender: Fighter, damage: Int): Boolean {
    // Exemplo simples: sempre retorna true por enquanto
    // Futuramente aqui você pode verificar conflitos reais
    return true
  }
}

/**
 * Validador que bloqueia ataques se o defensor estiver invulnerável.
 */
class InvulnerabilityValidator : Validator {
  override fun validate(attacker: Fighter, defender: Fighter, damage: Int): Boolean {
    val isInvulnerable = defender.attributes.get("invulnerable") > 0
    return !isInvulnerable
  }
}

/**
 * Validador que verifica efeitos ativos de itens antes de aplicar dano ou habilidades.
 *
 * Pode bloquear ou modificar ações dependendo dos efeitos do item.
 */
class ItemEffectValidator : Validator {
  override fun validate(attacker: Fighter, defender: Fighter, damage: Int): Boolean {
    // Exemplo simples: se o defensor tiver invulnerabilidade ativa por item
    val invulnerableTurns = defender.attributes.get("invulnerable")
    if (invulnerableTurns > 0) {
      println("${defender.name} é invulnerável devido a item! Ataque bloqueado.")
      return false
    }

    // Aqui você podem ser adicionadas checagens de outros efeitos de itens
    // Ex: buffs que anulam ataques, redução de dano por armadura especial, etc.

    return true
  }
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

/**
 * Exemplo de uso:
 *
 * val validators = listOf(HPValidator(), BuffConflictValidator(), InvulnerabilityValidator())
 * applyDamageWithValidation(warrior, mage, 15, validators)
 */
