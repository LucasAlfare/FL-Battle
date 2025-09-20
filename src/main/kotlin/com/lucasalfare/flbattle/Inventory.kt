package com.lucasalfare.flbattle

/**
 * Representa o inventário de um Fighter.
 *
 * Um Fighter pode possuir múltiplos itens.
 */
class Inventory {
  private val items: MutableList<Item> = mutableListOf()

  /** Adiciona um item ao inventário */
  fun addItem(item: Item) {
    items.add(item)
    println("Item '${item.name}' adicionado ao inventário")
  }

  /** Remove um item do inventário */
  fun removeItem(item: Item) {
    items.remove(item)
    println("Item '${item.name}' removido do inventário")
  }

  /** Lista todos os itens presentes no inventário */
  fun listItems() {
    println("Inventário:")
    items.forEach { println("- ${it.name}: ${it.description}") }
  }

  /** Retorna a lista de itens para uso em combate ou fora dele */
  fun getItems(): List<Item> = items
}