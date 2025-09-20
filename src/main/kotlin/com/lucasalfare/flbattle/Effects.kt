package com.lucasalfare.flbattle

/**
 * Interface que representa um efeito de item.
 * Todo efeito deve implementar a função apply, que aplica a lógica ao Fighter alvo.
 */
interface Effect {
  /**
   * Aplica o efeito ao Fighter alvo.
   *
   * @param target Fighter que receberá o efeito
   */
  fun apply(target: Fighter)
}

/**
 * Efeito que aumenta ou diminui um atributo específico de um Fighter.
 *
 * Pode ser usado tanto para itens passivos quanto consumíveis.
 *
 * @param attribute Nome do atributo a ser alterado (ex: "strength", "defense", "hp")
 * @param amount Quantidade a ser adicionada (negativo para reduzir)
 */
class AttributeBoostEffect(private val attribute: String, private val amount: Int) : Effect {
  override fun apply(target: Fighter) {
    target.attributes.add(attribute, amount)
    println("${target.name} recebeu efeito de $attribute: $amount (novo valor: ${target.attributes.get(attribute)})")
  }
}

/**
 * Efeito que restaura HP de um Fighter.
 *
 * Pode ser usado em consumíveis como poções.
 *
 * @param amount Quantidade de HP a ser restaurada
 */
class HpRestoreEffect(private val amount: Int) : Effect {
  override fun apply(target: Fighter) {
    val originalHp = target.hp
    target.hp += amount
    println("${target.name} recuperou ${target.hp - originalHp} HP (HP atual: ${target.hp})")
  }
}

/**
 * Efeito que aplica invulnerabilidade temporária.
 *
 * Pode ser usado para buffs defensivos em combate.
 *
 * @param turns Quantidade de turnos que o efeito dura
 */
class InvulnerabilityEffect(private val turns: Int) : Effect {
  override fun apply(target: Fighter) {
    target.attributes.set("invulnerable", turns)
    println("${target.name} está invulnerável por $turns turnos")
  }
}

/**
 * Efeito de exemplo que aplica dano direto a um alvo.
 *
 * Pode ser usado para consumíveis ofensivos ou itens mágicos.
 *
 * @param amount Quantidade de dano
 */
class DirectDamageEffect(private val amount: Int) : Effect {
  override fun apply(target: Fighter) {
    target.receiveDamage(amount, target) // pode ser modificado para attacker real se necessário
  }
}