/**
 * Item.kt
 *
 * Arquivo que define a estrutura modular de itens e efeitos para o protótipo de combate.
 *
 * -------------------
 * MOTIVAÇÃO
 * -------------------
 * Todos os efeitos e buffs gerados por itens (passivos ou ativos) são encapsulados
 * de forma independente. Itens podem ser aplicados ao próprio personagem ou a outros,
 * sem quebrar a arquitetura modular de Fighter, Rules ou Validators.
 *
 * -------------------
 * CONCEITOS
 * -------------------
 * 1. Item: encapsula nome, descrição e lista de efeitos.
 * 2. Effect: interface que define qualquer efeito que pode ser aplicado a um Fighter.
 *    Pode modificar atributos, adicionar Rules ou Validators, ou qualquer lógica customizada.
 * 3. Uso de item: aplica todos os efeitos contidos ao Fighter alvo.
 * 4. Efeitos passivos: simplesmente aplicam um efeito que dura enquanto o item estiver "equipado".
 * 5. Efeitos ativos: aplicam-se temporariamente, por exemplo durante o combate.
 */

package com.lucasalfare.flbattle

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Representa um item do jogo.
 *
 * Um item possui:
 * - Nome e descrição
 * - Lista de efeitos que serão aplicados ao usar o item
 */
class Item(
  val name: String,
  val description: String,
  val effects: List<Effect>
) {
  private val logger: Logger = LoggerFactory.getLogger(Item::class.java)

  /**
   * Aplica todos os efeitos do item ao Fighter alvo.
   *
   * Pode ser usado em si mesmo ou em outro Fighter.
   *
   * @param target Fighter que receberá os efeitos do item
   */
  fun use(target: Fighter) {
    logger.info("${target.name} usou o item $name")
    effects.forEach { it.apply(target) }
  }
}