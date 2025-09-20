package com.lucasalfare.flbattle

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
class Fighter(
  val name: String,
  val attributes: Attributes,
  val rules: List<Rule>
) {
  /** HP atual do personagem */
  var hp = attributes.get("hp")

  /** Inventário do personagem */
  val inventory = Inventory()

  /**
   * Realiza ataque a um alvo, aplicando todas as regras e validadores.
   *
   * @param target Fighter que receberá o ataque
   * @param validators Lista de validadores ativos neste combate
   */
  fun attack(target: Fighter, validators: List<Validator>) {
    rules.forEach { rule ->
      val damage = rule.calculate(this, target)
      applyDamageWithValidation(this, target, damage, validators)
    }
  }

  /**
   * Recebe dano de um atacante.
   *
   * @param amount Valor de dano final a ser aplicado
   * @param attacker Fighter que causou o dano
   */
  fun receiveDamage(amount: Int, attacker: Fighter) {
    hp -= amount
    println("$name took $amount damage from ${attacker.name} (HP: $hp)")
  }

  /** Retorna true se o personagem ainda estiver vivo */
  fun isAlive() = hp > 0

  /**
   * Usa um item do inventário em si mesmo ou em outro Fighter.
   *
   * @param item Item a ser usado
   * @param target Fighter que receberá os efeitos do item
   */
  fun useItem(item: Item, target: Fighter) {
    if (!inventory.getItems().contains(item)) {
      println("${name} não possui o item '${item.name}' no inventário")
      return
    }
    item.use(target)
  }
}