package com.lucasalfare.flbattle

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Representa um personagem do jogo (player ou inimigo), agora com inventário e capacidade de usar itens.
 *
 * @property name Nome do personagem
 * @property attributes Conjunto de atributos modulares
 * @property rules Lista de regras que definem como o personagem causa dano
 *
 * Responsabilidades:
 * - Manter atributos
 * - Aplicar regras de ataque
 * - Receber dano
 * - Gerenciar inventário e uso de itens
 */
data class Fighter(
  val name: String,
  val attributes: Attributes,
  val rules: List<Rule>
) {

  private val logger: Logger = LoggerFactory.getLogger(Fighter::class.java)

  /** Inventário do personagem */
  val inventory = Inventory()

  /**
   * Realiza ataque a um alvo, aplicando todas as regras e validadores.
   *
   * TODO: Múltiplas "regras" são aplicadas em sequência, talvez seja interessante pensar nessa aplicação de outra forma?
   *
   * @param target Fighter que receberá o ataque
   * @param validators Lista de validadores ativos neste combate
   */
  fun attack(target: Fighter, validators: List<Validator>) {
    rules.forEach { rule ->
      val rawDamage = rule.calculate(attacker = this, defender = target)
      applyDamageWithValidation(attacker = this, defender = target, damage = rawDamage, validators = validators)
    }
  }

  /**
   * Recebe dano de um atacante.
   *
   * @param amount Valor de dano final a ser aplicado
   * @param attacker Fighter que causou o dano
   */
  fun receiveDamage(amount: Int, attacker: Fighter) {
    setHp(amount = getHp() - amount)
    logger.info("$name got $amount damage from ${attacker.name} (HP: ${getHp()})")
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
    if (validators.all { it.validate(attacker = attacker, defender = defender, rawDamage = damage) }) {
      defender.receiveDamage(amount = damage, attacker = attacker)
    } else {
      logger.warn("Ação bloqueada por validação (ataque de ${attacker.name} a ${defender.name})")
    }
  }

  /** Retorna true se o personagem ainda estiver vivo */
  fun isAlive() = getHp() > 0

  /**
   * Usa um item do inventário em si mesmo ou em outro Fighter.
   *
   * @param item Item a ser usado
   * @param target Fighter que receberá os efeitos do item
   */
  fun useItem(item: Item, target: Fighter) {
    if (!inventory.getItems().contains(item)) {
      logger.info("$name não possui o item '${item.name}' no inventário")
      return
    }
    item.use(target)
  }

  fun getHp(): Int = attributes.get("hp")
  fun setHp(amount: Int) {
    attributes.set("hp", amount)
  }
}