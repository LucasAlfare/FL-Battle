/**
 * Jogo Modular de Combate Turn-Based (Rascunho)
 *
 * ------------------- MOTIVAÇÃO -------------------
 * Este projeto é um protótipo pessoal inspirado em MMORPGs medievais e jogos
 * de estratégia em turnos. O objetivo é construir uma base modular que permita
 * evoluir gradualmente, adicionando personagens, habilidades, buffs e interações
 * complexas, sem precisar refatorar o núcleo do combate.
 *
 * A ideia inicial é criar um sistema text-based para prototipagem rápida e
 * testes, mas com capacidade de expandir para PvE, PvP, múltiplos participantes
 * e eventualmente interface gráfica.
 *
 * ------------------- IDEIAS CENTRAIS -------------------
 * 1. **Fighters modulares**: Cada personagem possui atributos genéricos
 *    (hp, strength, defense, intelligence, magic_resist, etc.) armazenados
 *    em um dicionário, permitindo fácil extensão e ajustes.
 *
 * 2. **Regras de combate desacopladas (Rule)**: Ataque, defesa e cálculo de
 *    dano são implementados por regras independentes. Cada personagem pode ter
 *    regras diferentes, como ataque físico, mágico, crítico ou elemental.
 *
 * 3. **Turn-based simples**: O combate alterna turnos entre os combatentes.
 *    Pode ser expandido para múltiplos lutadores, parties ou inimigos simultâneos.
 *
 * 4. **Sistema de Validators**: Uma camada separada de validação garante que
 *    interações especiais ou restrições do jogo sejam respeitadas antes que
 *    qualquer ação seja aplicada. Exemplos:
 *      - HPValidator: evita HP negativo
 *      - BuffConflictValidator: checa conflitos entre buffs/debuffs
 *      - InvulnerabilityValidator: impede dano se personagem estiver invulnerável
 *
 *    Essa separação mantém Fighter e Battle independentes da lógica de restrição,
 *    e permite adicionar novas regras de validação sem alterar o núcleo do combate.
 *
 * 5. **Dano ofensivo x dano defensivo**:
 *      - `attack()` calcula o dano potencial do atacante, incluindo regras e buffs
 *      - `receiveDamage()` ajusta o dano com base na defesa, resistências e validadores
 *    Isso garante modularidade máxima e facilita balanceamento futuro.
 *
 * ------------------- VISÃO PARA O JOGO FINAL -------------------
 * - Pode evoluir para algo semelhante a *Final Fantasy Tactics*, *Divinity: Original Sin*
 *   ou *Heroes of Might & Magic*, mas começando como text-based simplificado.
 * - Players poderão criar personagens únicos, combinando atributos, regras e buffs.
 * - O sistema permite:
 *      * Combate PvE e PvP flexível
 *      * Habilidades físicas, mágicas, elementais ou combinadas
 *      * Buffs, debuffs e efeitos temporários
 *      * Regras de interação entre personagens e habilidades complexas
 *
 * ------------------- RESUMO -------------------
 * Este protótipo é um "laboratório de combate" modular:
 * - Core simples e legível
 * - Separação clara entre atributos, regras de dano e validações
 * - Expansível para buffs, habilidades e interações especiais
 * - Testável e previsível: qualquer novo efeito ou habilidade pode ser testado
 *   isoladamente usando regras e validadores
 *
 * O objetivo é permitir que, no futuro, seja possível criar novas mecânicas sem quebrar
 * a base, mantendo o combate coerente e modular.
 */
package com.lucasalfare.flbattle

