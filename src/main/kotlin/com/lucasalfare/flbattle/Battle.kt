package com.lucasalfare.flbattle

/**
 * Controla um combate por turnos entre dois [Fighter].
 *
 * **Modelo**
 * - A classe atua como uma *máquina de estados finitos (FSM)* com fases bem definidas
 *   (ver [Phase]).
 * - O controle do progresso do combate é externo: chamadas a [advancePhase] movem a
 *   FSM para a próxima fase. Não há loops internos; isto mantém a classe testável e
 *   integrável com UIs, engines ou simulações passo-a-passo.
 *
 * **Responsabilidades**
 * - Manter referências aos dois combatentes (internamente `attacker` e `defender`).
 * - Expor o estado atual ([phase]) e permitir registro de callbacks por fase.
 * - Aplicar validações de ataque via lista de [Validator].
 * - Encerrar o combate automaticamente se um [Fighter] morrer.
 *
 * **Invariantes**
 * - Ao final de qualquer operação pública, `phase` reflete a fase atual corretamente
 *   e callbacks da fase foram disparados.
 * - Quando `phase == Phase.FINISHED`, pelo menos um dos fighters não está vivo
 *   (a menos que [forceFinish] tenha sido chamado explicitamente).
 * - `currentAttacker` e `currentDefender` sempre retornam referências consistentes
 *   entre si (swapped via [swapTurn]).
 *
 * **Thread-safety**: esta implementação **não é** thread-safe. Se for necessário o
 * acesso concorrente, proteja chamadas externas com sincronização adequada.
 *
 * @param fighterA primeiro combatente; mantido como referência de origem.
 * @param fighterB segundo combatente; mantido como referência de origem.
 * @param validators lista de validadores a serem aplicados a cada ataque; pode ser vazia.
 *
 * @see Phase para as fases disponíveis.
 */
data class Battle(
  private val fighterA: Fighter,
  private val fighterB: Fighter,
  private val validators: List<Validator> = emptyList()
) {
  /**
   * Fases possíveis da batalha. Ordem lógica (não necessariamente ordinal):
   *
   * 1. TURN_START — inicialização do turno do atacante atual.
   * 2. PRE_ITEM — fase em que o atacante pode usar itens.
   * 3. ACTION — fase para executar o ataque (aplica [Validator]s).
   * 4. POST_ACTION — efeitos pós-ataque (atualizações externas, triggers, etc.).
   * 5. TURN_END — encerramento do turno; se o defensor sobrevive, ocorre swap.
   * 6. FINISHED — combate finalizado (um fighter morto ou interrupção).
   */
  enum class Phase { TURN_START, PRE_ITEM, ACTION, POST_ACTION, TURN_END, FINISHED }

  /** Atacante atual (mutável internamente apenas). */
  private var attacker: Fighter = fighterA

  /** Defensor atual (mutável internamente apenas). */
  private var defender: Fighter = fighterB

  /** Fase corrente. Modificada apenas por transições controladas. */
  var phase: Phase = Phase.TURN_START
    private set

  /** Atacante visível externamente (apenas leitura). */
  val currentAttacker: Fighter get() = attacker

  /** Defensor visível externamente (apenas leitura). */
  val currentDefender: Fighter get() = defender

  /**
   * Mapa de callbacks a serem executados ao entrar em cada fase. Mantemos uma
   * lista mutável por fase para garantir ordens previsíveis de execução.
   */
  private val callbacks: MutableMap<Phase, MutableList<(Battle) -> Unit>> =
    Phase.entries.associateWith { mutableListOf<(Battle) -> Unit>() }.toMutableMap()

  /**
   * Registra um callback que será executado sempre que a batalha entrar em [phase].
   *
   * O callback recebe a instância atual de [Battle]. Use com cautela: callbacks
   * podem inspecionar estado, mas **não devem** alterar as fases internamente —
   * alterações de fase devem ser feitas através da API pública ([advancePhase], [forceFinish], ...)
   * para preservar as invariantes.
   *
   * @param phase fase alvo para o callback.
   * @param callback função que recebe [Battle] e executa lógica associada.
   */
  fun on(phase: Phase, callback: (Battle) -> Unit) {
    callbacks[phase]?.add(callback)
  }

  /* ---------- Inicialização / ciclo de vida ---------- */

  /**
   * Inicializa a batalha: garante que `attacker` e `defender` apontem para [fighterA] e [fighterB]
   * e dispara o callback da fase inicial. Se um dos fighters já estiver morto
   * ao iniciar, a batalha entra diretamente em [Phase.FINISHED].
   *
   * Efeitos colaterais:
   *  - atualiza [phase];
   *  - dispara callbacks associados à fase final estabelecida.
   */
  fun begin() {
    attacker = fighterA
    defender = fighterB
    val startPhase = if (fighterA.isAlive() && fighterB.isAlive()) Phase.TURN_START else Phase.FINISHED
    nextPhase(startPhase)
  }

  /**
   * Avança a máquina de estados para a próxima fase lógica e retorna a fase
   * resultante.
   *
   * Regras de transição (resumidas):
   *  - TURN_START → PRE_ITEM
   *  - PRE_ITEM → ACTION
   *  - ACTION → POST_ACTION
   *  - POST_ACTION → TURN_END
   *  - TURN_END → (se defensor vivo: swap + TURN_START) ou FINISHED
   *  - FINISHED → FINISHED (noop)
   *
   * @return a [Phase] após a transição (pode ser igual à atual se FINISHED).
   */
  fun advancePhase(): Phase {
    if (phase == Phase.FINISHED) return phase

    val next = when (phase) {
      Phase.TURN_START -> Phase.PRE_ITEM
      Phase.PRE_ITEM -> Phase.ACTION
      Phase.ACTION -> Phase.POST_ACTION
      Phase.POST_ACTION -> Phase.TURN_END
      Phase.TURN_END -> {
        if (!defender.isAlive()) Phase.FINISHED
        else {
          swapTurn()
          Phase.TURN_START
        }
      }

      else -> Phase.FINISHED
    }

    nextPhase(next)
    return phase
  }

  /**
   * Retorna os itens disponíveis no inventário do atacante atual.
   *
   * Esta é uma simples delegação a `attacker.inventory.getItems()` para manter
   * callers desacoplados da estrutura interna de inventário.
   */
  fun availableItems(): List<Item> = attacker.inventory.getItems()

  /**
   * Usa [item] em [target]. Permitido apenas durante [Phase.PRE_ITEM].
   *
   * Contrato:
   *  - lança [IllegalStateException] se chamado fora de [Phase.PRE_ITEM].
   *  - após execução, verifica se o ataque ou o item causaram morte e, caso
   *    afirmativo, finaliza a batalha.
   *
   * @param item item a ser usado.
   * @param target alvo do item; padrão é o próprio atacante.
   */
  fun useItem(item: Item, target: Fighter = attacker) {
    if (phase != Phase.PRE_ITEM) {
      throw IllegalStateException("useItem() só é permitido em Phase.PRE_ITEM")
    }

    attacker.useItem(item, target)
    checkAndFinishIfNeeded()
  }

  /**
   * Executa um ataque do atacante atual contra o defensor.
   *
   * Contrato:
   *  - permitido apenas durante [Phase.ACTION]. Caso contrário, lança [IllegalStateException].
   *  - aplica os [validators] configurados ao ataque (via `attacker.attack(defender, validators)`).
   *  - se o defensor morrer como resultado, a batalha entra imediatamente em [Phase.FINISHED]
   *    e callbacks de finalização são disparados.
   *  - esta função **não** avança automaticamente para [Phase.POST_ACTION] quando o defensor
   *    sobrevive — o controle de avanço permanece com o chamador (consistente com o design
   *    de controle externo).
   *
   * @throws IllegalStateException se chamado fora de [Phase.ACTION].
   */
  fun attack() {
    if (phase != Phase.ACTION) {
      throw IllegalStateException("attack() só é permitido em Phase.ACTION")
    }

    attacker.attack(defender, validators)

    // Se defesa chegou a 0, finalizamos imediatamente.
    if (!defender.isAlive()) nextPhase(Phase.FINISHED)
  }

  /** Retorna `true` se a batalha está finalizada. */
  fun isFinished(): Boolean = phase == Phase.FINISHED

  /**
   * Retorna o vencedor da batalha ou `null` se não houver vencedor definido ainda.
   *
   * @return instância de [Fighter] vencedora ou `null` em empate/indefinido.
   */
  fun winner(): Fighter? = when {
    fighterA.isAlive() && !fighterB.isAlive() -> fighterA
    fighterB.isAlive() && !fighterA.isAlive() -> fighterB
    else -> null
  }

  /**
   * Finaliza imediatamente a batalha, independente da fase atual. Dispara callbacks.
   */
  fun forceFinish() = nextPhase(Phase.FINISHED)

  /**
   * Troca os papéis de atacante/defensor.
   *
   * Implementação deliberadamente explícita e simples para legibilidade. Não altera [phase].
   */
  private fun swapTurn() {
    val tmp = attacker
    attacker = defender
    defender = tmp
  }

  /**
   * Verifica o estado de vida dos combatentes e, se necessário, força [Phase.FINISHED].
   *
   * Este helper é utilizado quando ações (itens/efeitos) podem matar um combatente
   * sem necessariamente ocorrer durante a fase de ataque.
   */
  private fun checkAndFinishIfNeeded() {
    if (!fighterA.isAlive() || !fighterB.isAlive()) {
      if (phase != Phase.FINISHED) nextPhase(Phase.FINISHED)
    }
  }

  /** Dispara os callbacks registrados para a [phase] atual. */
  private fun fire() {
    callbacks[phase]?.forEach { it(this) }
  }

  /**
   * Centraliza a transição de fase e o disparo de callbacks.
   *
   * Uso preferencial para garantir que toda mudança de [phase] passe pelo mesmo
   * ponto (evita duplicação de efeitos colaterais). Não faz validações complexas
   * — a política de transição está em [advancePhase].
   */
  private fun nextPhase(next: Phase) {
    phase = next
    fire()
  }
}