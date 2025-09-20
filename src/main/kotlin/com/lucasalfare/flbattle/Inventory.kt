package com.lucasalfare.flbattle

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Representa o inventário de um Fighter.
 *
 * Um Fighter pode possuir múltiplos itens.
 */
class Inventory {
  private val logger: Logger = LoggerFactory.getLogger(Inventory::class.java)

  private val items: MutableList<Item> = mutableListOf()

  /** Adiciona um item ao inventário */
  fun addItem(item: Item) {
    items.add(item)
    logger.info("Item '${item.name}' adicionado ao inventário")
  }

  /** Remove um item do inventário */
  fun removeItem(item: Item) {
    items.remove(item)
    logger.info("Item '${item.name}' removido do inventário")
  }

  /** Lista todos os itens presentes no inventário */
  fun listItems() {
    logger.info("Inventário:")
    items.forEach { println("- ${it.name}: ${it.description}") }
  }

  /** Retorna a lista de itens para uso em combate ou fora dele */
  fun getItems(): List<Item> = items
}