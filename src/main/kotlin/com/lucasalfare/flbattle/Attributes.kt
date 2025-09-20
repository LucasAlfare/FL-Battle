package com.lucasalfare.flbattle

/**
 * Representa um conjunto de atributos de um personagem de forma modular.
 *
 * Cada atributo é um par chave/valor, permitindo adicionar ou modificar atributos
 * sem alterar a classe Fighter.
 *
 * Exemplo de atributos: "hp", "strength", "defense", "intelligence", "magic_resist".
 */
class Attributes(private val values: MutableMap<String, Int> = mutableMapOf()) {

  /** Retorna o valor atual de um atributo, 0 se não existir. */
  fun get(name: String): Int = values[name] ?: 0

  /** Define o valor de um atributo. */
  fun set(name: String, value: Int) {
    values[name] = value
  }

  /** Adiciona um delta a um atributo existente ou cria ele se não existir. */
  fun add(name: String, delta: Int) {
    values[name] = get(name) + delta
  }
}