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