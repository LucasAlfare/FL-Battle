# FL-Battle: Modular Combat Engine for Games (Kotlin Prototype)

## Overview

This prototype serves as a base for a combat system inspired by medieval MMORPGs such as Final Fantasy, RuneScape, World of Warcraft, and MOBA-style games like League of Legends. Current features include:

* **Turn-based combat**
* **Characters with modular attributes**
* **Flexible attack rules**
* **Items with passive and active effects**
* **Validators for consistency and complex interactions**

The goal is to create a **highly modular, scalable, and extensible architecture**, allowing testing, rapid prototyping, and easy integration of new features. This provides a foundation for experimenting with any gameplay ideas, including card games, text-based combat games, or other custom systems.

The main challenge is balancing simplicity with abstraction. Excessive abstraction can hinder usability, but the current draft strikes a functional modular balance.

LLMs were partially used to generate code snippets, so the project remains a raw prototype. Updates will progressively refine it into a more polished system.

---

## Motivation

* Build a flexible combat system without hardcoded values.
* Support creation of unique characters with custom attributes and abilities.
* Ensure modularity for items, buffs, and debuffs.
* Implement validation layers to handle complex interactions and prevent inconsistencies.
* Serve as a foundation for future expansions: PvP, PvE, group battles, special abilities, and strategic items.

---

## Prototype Components

Below are the core components with **usage examples and implementation flow**.

---

### 1. **[Attributes](src/main/kotlin/com/lucasalfare/flbattle/Attributes.kt)**

* **Purpose:** Store and manage character attributes modularly.
* **Usage:**

    * Define HP, strength, defense, magic resistance, agility, etc.
    * Serve as a base for damage and item effect calculations.
    * Support dynamic updates: buffs, debuffs, equipped items, temporary effects.
* **Implementation flow:**

    1. Instantiate `Attributes` when creating a Fighter.
    2. Add key/value pairs for initial attributes.
    3. Access and modify values dynamically during attacks, item effects, or buffs.
* **Example:**

  ```kotlin
  val warriorAttributes = Attributes(mutableMapOf("hp" to 100, "strength" to 15, "defense" to 10))
  warriorAttributes.add("strength", 5) // item or ability buff
  val currentHp = warriorAttributes.get("hp")
  ```

---

### 2. **[Rule](src/main/kotlin/com/lucasalfare/flbattle/Rules.kt)**

* **Purpose:** Modularize damage and ability logic.
* **Usage:**

    * Each Fighter can have multiple attack rules (physical, magical, critical, special).
    * Change calculation logic without modifying Fighter or Battle classes.
* **Implementation flow:**

    1. Implement `Rule` interface for each attack type.
    2. Define `calculate(attacker, defender)` using attributes.
    3. Return final value, optionally processed by validators.
* **Example:**

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

* **Purpose:** Represent a complete character with attributes, rules, HP, and inventory.
* **Usage:**

    * Centralizes attack, damage reception, and item usage logic.
    * Holds an inventory for items affecting attributes, HP, or rules.
    * Maintains state (HP, buffs/debuffs, active effects).
* **Implementation flow:**

    1. Create Fighter with Attributes and Rules.
    2. Add items to Inventory.
    3. Call `attack(target, validators)` during Battle.
    4. Apply item and buff effects before or after attacks.
* **Example:**

  ```kotlin
  val warrior = Fighter("Warrior", warriorAttributes, listOf(PhysicalAttackRule()))
  val potion = Item("Potion", "Restores 20 HP", listOf(HpRestoreEffect(20)))
  warrior.inventory.addItem(potion)
  warrior.useItem(potion, warrior)
  ```

---

### 4. **[Battle](src/main/kotlin/com/lucasalfare/flbattle/Battle.kt)**

* **Purpose:** Implement a finite state machine (FSM) controlling turn-based combat between two Fighters.
  Tracks combat state, exposes phases, and supports UI, testing, and external simulation integration.

* **Features:**

    * Explicit phase cycle: `TURN_START`, `PRE_ITEM`, `ACTION`, `POST_ACTION`, `TURN_END`, `FINISHED`.
    * Manual flow control — caller decides phase progression.
    * Phase callbacks: `on(phase) { ... }`.
    * Rule integration via Validators.
    * Automatic combat end detection when a Fighter dies.

* **Usage:**

    * Alternates attacker/defender deterministically.
    * Supports item use and attacks at specific phases.
    * Provides a testable, simple API through `advancePhase()`.

* **Implementation flow:**

    1. Create `Battle(f1, f2, validators)`.
    2. Register callbacks with `on(Phase.X) { battle -> /* reaction */ }`.
    3. Call `begin()` to start combat.
    4. Progress phases with `advancePhase()`.
    5. Use items in `Phase.PRE_ITEM` and attacks in `Phase.ACTION`.
    6. Repeat until `Phase.FINISHED` or `finishNow()`.

* **Example:**

  ```kotlin
  val battle = Battle(warrior, mage, validators)

  battle.on(Battle.Phase.TURN_START) {
    println("Turn of ${it.currentAttacker.name}")
  }

  battle.on(Battle.Phase.FINISHED) {
    println("Winner: ${it.winner()?.name ?: "(none)"}")
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

* **Purpose:** Ensure actions respect game rules and maintain consistency in complex interactions.
* **Usage:**

    * Prevent invalid attacks (e.g., invulnerable target)
    * Avoid buff/item effect conflicts
    * Check conditions before applying temporary effects
* **Implementation flow:**

    1. Implement `Validator` interface.
    2. Implement `validate(attacker, defender, damage)` or similar methods.
    3. Add to Battle validators list.
* **Example:**

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

* **Purpose:** Encapsulate any effect applied to a Fighter, modular and reusable.
* **Usage:**

    * Item, ability, buff, or debuff effects.
    * Can modify attributes, add rules, or interact with Validators.
* **Implementation flow:**

    1. Implement `Effect` interface.
    2. Define `apply(target: Fighter)` to modify target state.
    3. Assign effects to items or abilities.
* **Example:**

  ```kotlin
  class HpRestoreEffect(private val amount: Int) : Effect {
      override fun apply(target: Fighter) {
          target.hp += amount
      }
  }
  ```

---

### 7. **[Item](src/main/kotlin/com/lucasalfare/flbattle/Items.kt)**

* **Purpose:** Represent objects that can modify Fighter attributes or states.
* **Usage:**

    * Includes name, description, and list of Effects.
    * Can target self or another Fighter.
    * Supports consumables, equipment, and strategic items.
* **Implementation flow:**

    1. Create item with desired effects.
    2. Add to Fighter inventory.
    3. Use item with `useItem(item, target)` during turn.
* **Example:**

  ```kotlin
  val sword = Item("Sword of Strength", "Increases strength", listOf(AttributeBoostEffect("strength", 5)))
  warrior.inventory.addItem(sword)
  warrior.useItem(sword, warrior)
  ```

---

### 8. **[Inventory](src/main/kotlin/com/lucasalfare/flbattle/Inventory.kt)**

* **Purpose:** Organize and manage a Fighter's items centrally.
* **Usage:**

    * Add, remove, and list items.
    * Enable strategic item use during combat.
* **Implementation flow:**

    1. Create Inventory in Fighter.
    2. Add items to inventory.
    3. List or use items as needed per turn.
* **Example:**

  ```kotlin
  val inventory = Inventory()
  inventory.addItem(potion)
  inventory.listItems()
  ```

---

## Combat Flow Example

1. Turn starts → active Fighter decides action
2. Apply passive or active items (`Effect.apply()`)
3. Calculate attack via Rule (`calculate`)
4. Apply Validators for consistency
5. Apply damage or effect (`Fighter.receiveDamage()` or `Effect`)
6. Turn ends → next Fighter
7. Repeat until a winner is determined