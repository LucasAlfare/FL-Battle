> Read in english [here](README.english.md)

# FL-Battle: Engine modular de Combate para Jogos
[![](https://jitpack.io/v/LucasAlfare/FL-Battle.svg)](https://jitpack.io/#LucasAlfare/FL-Battle)

## Resumo

Estou desenvolvendo este protótipo para servir como base de um sistema de combate inspirado em MMORPGs medievais estilo Final Fantasy, RuneScape, World of Warcraft, League of Legends. Por enquanto minhas ideias contam com:

* **Turnos alternados** (turn-based)
* **Personagens com atributos modulares**
* **Regras de ataque flexíveis**
* **Itens com efeitos passivos e ativos**
* **_Validators_ para consistência e interações complexas**

O objetivo é criar uma **arquitetura o mais _modular_ possível**, escalável e extensível, permitindo testes, prototipagem rápida e fácil integração de novos recursos. Estou fazendo isso pois quero ter em mãos algo que me permita brincar de implementar jogos com qualquer tipo de ideia que eu vier a ter em mente. Mesmo que eu não implemente jogos como os de exemplo que citei acima, vou poder implementar coisas como jogos de cartas, jogos de combate baseados em texto, etc.

Essa arquitetura modular que tô propondo tem o desafio de a gente precisar saber o trade-off/limite/balanço entre o quanto deixar simples e direto e o quanto abstrair. Abstrair de mais nunca é uma boa ideia, mas para uma modularização aceitável acredito que o ponto em que cheguei no meu rascunho deva estar _"ok"_.

Em boa parte da elaboração utilizei _LLMs_ para completarem código para mim, baseado em minhas requisições, por isso trata-se, ainda, de um protótipo bruto. À medida que eu for atualizando o código por mim mesmo, vamos poder sair da fase de protótipo.

---

## Como experimentar

Estou disponibilizando esse projeto através do [Jitpack](https://jitpack.io/#LucasAlfare/FL-Battle/Tag):

```kotlin
// in your build.gradle.kts
repositories {
  maven("http://jitpack.io")
}

dependencies {
  implementation("com.github.LucasAlfare:FL-Battle:1.0-alpha-1")
}
```

Caso esteja utilizando Maven outro sistema de build, acesse o link acima do Jitpack para mais detalhes de como incluir esse projeto sem precisar baixa-lo.

## Componentes do Protótipo

A seguir, veja os detalhes de cada componente que coloquei em código, com **exemplos de uso prático e fluxo de implementação**.

---

### 1. **[Attributes](src/main/kotlin/com/lucasalfare/flbattle/Attributes.kt)**

* **Propósito:** Armazenar e gerenciar atributos de personagens de forma modular.
* **Uso prático:**
    * Determinar pontos de vida, força, defesa, resistência mágica, agilidade, etc.
    * Servir como base para cálculos de dano e efeitos de itens.
    * Permitir alteração dinâmica: buffs, debuffs, itens equipados, efeitos temporários.
* **Fluxo de implementação:**
    1. Criar uma instância de Attributes ao criar o Fighter.
    2. Adicionar pares chave/valor representando os atributos iniciais.
    3. Sempre que um ataque, efeito de item ou buff é aplicado, acessar e modificar esses valores dinamicamente.
* **Exemplo prático:**
  ```kotlin
  val warriorAttributes = Attributes(mutableMapOf("hp" to 100, "strength" to 15, "defense" to 10))
  warriorAttributes.add("strength", 5) // buff de item ou habilidade
  val currentHp = warriorAttributes.get("hp")
  ```

---

### 2. **[Rule](src/main/kotlin/com/lucasalfare/flbattle/Rules.kt)**

* **Propósito:** Modularizar a lógica de cálculo de dano e habilidades.
* **Uso prático:**
    * Cada Fighter pode ter múltiplas regras de ataque (físico, mágico, crítico, especial).
    * Permite alterar a forma de cálculo sem tocar em Fighter ou Battle.
* **Fluxo de implementação:**
    1. Implementar a interface `Rule` para cada tipo de ataque.
    2. Dentro do método `calculate(attacker, defender)`, usar atributos para definir o dano ou efeito.
    3. Retornar o valor final que será aplicado ao alvo, possivelmente passando por validators.
* **Exemplo prático:**
  ```kotlin
  class PhysicalAttackRule : Rule {
      override fun calculate(attacker: Fighter, defender: Fighter): Int {
          val baseDamage = attacker.attributes.get("strength")
          val damageReduced = baseDamage - defender.attributes.get("defense")
          return maxOf(damageReduced, 0)
      }
  }
  ```

---

### 3. **[Fighter](src/main/kotlin/com/lucasalfare/flbattle/Fighter.kt)**

* **Propósito:** Representar um personagem completo com atributos, regras, HP e inventário.
* **Uso prático:**
    * Centraliza a lógica de ataque, recebimento de dano e uso de itens.
    * Possui inventário para armazenar itens que afetam atributos, HP ou regras.
    * Mantém estado (HP, buffs/debuffs, efeitos ativos).
* **Fluxo de implementação:**
    1. Criar Fighter com Attributes e Rules.
    2. Adicionar itens ao Inventory.
    3. Chamar `attack(target, validators)` durante Battle.
    4. Aplicar efeitos de itens e buffs antes ou depois do ataque.
* **Exemplo prático:**
  ```kotlin
  val warrior = Fighter("Warrior", warriorAttributes, listOf(PhysicalAttackRule()))
  val potion = Item("Potion", "Recupera 20 HP", listOf(HpRestoreEffect(20)))
  warrior.inventory.addItem(potion)
  warrior.useItem(potion, warrior) // usa em si mesmo
  ```

---

### 4. **[Battle](src/main/kotlin/com/lucasalfare/flbattle/Battle.kt)**

**Propósito:** Implementar uma máquina de estados finita (FSM) que controla um combate por turnos
entre dois `Fighter`s. A classe mantém o estado do combate, expõe fases nominais
e permite integração com UIs, testes e simulações externas.

**Características principais:**
* Ciclo de fases explícitas: `TURN_START`, `PRE_ITEM`, `ACTION`, `POST_ACTION`, `TURN_END`, `FINISHED`.
* Controle externo do fluxo: nenhuma iteração automática — chamador decide quando avançar.
* Callbacks por fase: `on(phase) { ... }` para reagir à entrada em fases.
* Integração de regras via `Validator`s fornecidos no construtor.
* Reconhecimento automático de fim de combate quando um `Fighter` morre.

**Uso prático:**
* Alterna turnos entre atacante/defensor de forma explícita e determinística.
* Permite uso de itens e execução de ataques em fases específicas.
* Mantém API simples e testável — todas as transições passam por `advancePhase()`.


**Fluxo de implementação:**
1. Criar `Battle(f1, f2, validators)`.
2. Registrar callbacks com `on(Phase.X) { battle -> /* reação */ }` se necessário.
3. Chamar `begin()` para inicializar o combate.
4. Avançar fases manualmente com `advancePhase()`.
5. Durante `Phase.PRE_ITEM` usar `useItem()`; durante `Phase.ACTION` usar `attack()`.
6. Repetir chamadas a `advancePhase()` até `Phase.FINISHED` ou chamar `finishNow()`.

**Exemplo prático (padrão de uso):**

```kotlin
val battle = Battle(warrior, mage, validators)

battle.on(Battle.Phase.TURN_START) {
  println("Turno de ${it.currentAttacker.name}")
}

battle.on(Battle.Phase.FINISHED) {
  println("Vencedor: ${it.winner()?.name ?: "(nenhum)"}")
}

battle.begin()

while (!battle.isFinished()) {
  when (battle.phase) {
    Battle.Phase.PRE_ITEM -> battle.advancePhase()
    Battle.Phase.ACTION -> {
      battle.attack()
      if (!battle.isFinished()) battle.advancePhase()
    }
    else -> battle.advancePhase()
  }
}
```

---

### 5. **[Validator](src/main/kotlin/com/lucasalfare/flbattle/Validators.kt)**
* **Propósito:** Garantir que ações respeitem regras do jogo e que interações complexas de buffs, debuffs ou itens sejam consistentes.
* **Uso prático:**
    * Evitar ataques inválidos (ex.: alvo invulnerável)
    * Prevenir conflitos de buffs ou efeitos de itens
    * Checar condições antes de aplicar efeitos temporários
* **Fluxo de implementação:**
    1. Criar classe que implementa `Validator`.
    2. Implementar `validate(attacker, defender, damage)` ou métodos equivalentes.
    3. Adicionar à lista de validators do Battle.
* **Exemplo prático:**
  ```kotlin
  class ItemEffectValidator : Validator {
      override fun validate(attacker: Fighter, defender: Fighter, damage: Int): Boolean {
          val invulnerableTurns = defender.attributes.get("invulnerable")
          return invulnerableTurns <= 0
      }
  }
  ```

---

### 6. **[Effect](src/main/kotlin/com/lucasalfare/flbattle/Effects.kt)**

* **Propósito:** Encapsular lógica de qualquer efeito aplicado a um Fighter, de forma modular e reutilizável.
* **Uso prático:**
    * Efeitos de itens, habilidades, buffs e debuffs
    * Podem alterar atributos, adicionar regras ou interagir com Validators
* **Fluxo de implementação:**
    1. Criar classe que implementa `Effect`.
    2. Implementar `apply(target: Fighter)` para modificar estado do alvo.
    3. Associar efeitos a itens ou habilidades.
* **Exemplo prático:**
  ```kotlin
  class HpRestoreEffect(private val amount: Int) : Effect {
      override fun apply(target: Fighter) {
          target.hp += amount
      }
  }
  ```

---

### 7. **[Item](src/main/kotlin/com/lucasalfare/flbattle/Items.kt)**
* **Propósito:** Representar objetos que podem alterar atributos ou estados de um Fighter.
* **Uso prático:**
    * Contém nome, descrição e lista de Effects
    * Pode ser usado em si mesmo ou em outro Fighter
    * Suporta consumíveis, equipamentos ou itens estratégicos
* **Fluxo de implementação:**
    1. Criar item com efeitos desejados.
    2. Adicionar ao inventário de Fighter.
    3. Durante turno, chamar `useItem(item, target)`.
* **Exemplo prático:**
  ```kotlin
  val sword = Item("Sword of Strength", "Aumenta força", listOf(AttributeBoostEffect("strength", 5)))
  warrior.inventory.addItem(sword)
  warrior.useItem(sword, warrior)
  ```

---

### 8. **[Inventory](src/main/kotlin/com/lucasalfare/flbattle/Inventory.kt)**

* **Propósito:** Organizar e gerenciar itens de um Fighter de forma centralizada.
* **Uso prático:**
    * Permite adição, remoção, listagem de itens
    * Facilita o uso estratégico de itens durante combate
* **Fluxo de implementação:**
    1. Criar Inventory dentro de Fighter.
    2. Adicionar itens ao inventário.
    3. Listar ou usar itens conforme necessidade do turno.
* **Exemplo prático:**
  ```kotlin
  val inventory = Inventory()
  inventory.addItem(potion)
  inventory.listItems()
  ```

---

## Ideia de Fluxo de Combate

1. Turno inicia → Fighter ativo decide ação
2. Aplicação de itens passivos ou ativos (`Effect.apply()`)
3. Ataque calculado via Rule (`calculate`)
4. Validators aplicados para checagem de consistência
5. Dano ou efeito aplicado (`Fighter.receiveDamage()` ou `Effect`)
6. Turno termina → próximo Fighter
7. Repetir até que um vencedor seja definido

# [LICENSE](LICENSE)
```
MIT License

Copyright (c) 2025 Francisco Lucas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```