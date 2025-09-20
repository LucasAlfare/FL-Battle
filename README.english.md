# FL-Battle: Modular Combat Engine for Games (Kotlin Prototype)

## Overview

I am developing this prototype as a base for a combat system inspired by medieval MMORPGs like Final Fantasy, RuneScape, World of Warcraft, League of Legends. Currently, my ideas include:

* **Alternating turns** (turn-based)
* **Characters with modular attributes**
* **Flexible attack rules**
* **Items with passive and active effects**
* ***Validators* for consistency and complex interactions**

The goal is to create the most possible **highly *modular* architecture**, scalable and extensible, allowing testing, rapid prototyping, and easy integration of new features. This gives me a framework to experiment with any game idea I may want to implement for myself. Even if I don’t make full-scale games like the examples above, I can implement card games, text-based combat games, etc.

The challenge of this modular architecture is finding the trade-off/limit/balance between simplicity and abstraction. Over-abstraction is never good, but for acceptable modularization, the current draft is likely *"ok"*.

Much of the initial development leveraged *LLMs* to complete code based on my instructions, so it is still a rough prototype. As I update the code manually, it will evolve beyond the prototype stage.

---

## More on Motivation

* Create a flexible combat system without hardcoded values.
* Enable creation of unique and varied characters with custom attributes and skills.
* Ensure modularity for items, buffs, and debuffs.
* Implement a validation layer for complex interactions and to prevent inconsistencies.
* Serve as a foundation for future expansion: PvP, PvE, group battles, special abilities, and strategic items.

---

## Prototype Components

Below are details of each component implemented in code, with **practical usage examples and implementation flow**.

---

### 1. **[Attributes](src/main/kotlin/com/lucasalfare/flbattle/Attributes.kt)**

* **Purpose:** Store and manage character attributes in a modular way.
* **Practical use:**

    * Determine health points, strength, defense, magic resistance, agility, etc.
    * Serve as a base for damage calculations and item effects.
    * Allow dynamic modification: buffs, debuffs, equipped items, temporary effects.
* **Implementation flow:**

    1. Create an `Attributes` instance when creating a `Fighter`.
    2. Add key/value pairs for initial attributes.
    3. Whenever an attack, item effect, or buff is applied, access and modify these values dynamically.
* **Example:**

  ```kotlin
  val warriorAttributes = Attributes(mutableMapOf("hp" to 100, "strength" to 15, "defense" to 10))
  warriorAttributes.add("strength", 5) // item or skill buff
  val currentHp = warriorAttributes.get("hp")
  ```

---

### 2. **[Rule](src/main/kotlin/com/lucasalfare/flbattle/Rules.kt)**

* **Purpose:** Modularize damage and skill calculation logic.
* **Practical use:**

    * Each `Fighter` can have multiple attack rules (physical, magical, critical, special).
    * Allows changing calculation logic without modifying `Fighter` or `Battle`.
* **Implementation flow:**

    1. Implement the `Rule` interface for each attack type.
    2. Use attributes in the `calculate(attacker, defender)` method to define damage or effect.
    3. Return the final value to apply to the target, possibly passing through validators.
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

* **Purpose:** Represent a full character with attributes, rules, HP, and inventory.
* **Practical use:**

    * Centralizes logic for attacking, taking damage, and using items.
    * Contains an inventory to store items affecting attributes, HP, or rules.
    * Maintains state (HP, buffs/debuffs, active effects).
* **Implementation flow:**

    1. Create `Fighter` with `Attributes` and `Rules`.
    2. Add items to `Inventory`.
    3. Call `attack(target, validators)` during `Battle`.
    4. Apply item effects and buffs before or after attack.
* **Example:**

  ```kotlin
  val warrior = Fighter("Warrior", warriorAttributes, listOf(PhysicalAttackRule()))
  val potion = Item("Potion", "Restores 20 HP", listOf(HpRestoreEffect(20)))
  warrior.inventory.addItem(potion)
  warrior.useItem(potion, warrior) // self-use
  ```

---

### 4. **[Battle](src/main/kotlin/com/lucasalfare/flbattle/Battle.kt)**

* **Purpose:** Manage turn-based battles between Fighters.
* **Practical use:**

    * Alternates turns between attacker and defender.
    * Applies attacks using `Rules` and validation via `Validators`.
    * Can integrate item usage during the turn.
* **Implementation flow:**

    1. Create `Battle` with two Fighters and a list of `Validators`.
    2. Start turn loop until one Fighter is defeated.
    3. During each turn, apply items, buffs, and attack.
* **Example:**

  ```kotlin
  val battle = Battle(warrior, mage, validators)
  battle.start()
  ```

---

### 5. **[Validator](src/main/kotlin/com/lucasalfare/flbattle/Validators.kt)**

* **Purpose:** Ensure actions follow game rules and that complex interactions of buffs, debuffs, or items are consistent.
* **Practical use:**

    * Prevent invalid attacks (e.g., invulnerable target)
    * Avoid conflicts between buffs or item effects
    * Check conditions before applying temporary effects
* **Implementation flow:**

    1. Create class implementing `Validator`.
    2. Implement `validate(attacker, defender, damage)` or equivalent methods.
    3. Add to `Battle`’s validator list.
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

* **Purpose:** Encapsulate logic for any effect applied to a Fighter in a modular, reusable way.
* **Practical use:**

    * Effects from items, skills, buffs, and debuffs
    * Can modify attributes, add rules, or interact with `Validators`
* **Implementation flow:**

    1. Create class implementing `Effect`.
    2. Implement `apply(target: Fighter)` to modify the target’s state.
    3. Associate effects with items or skills.
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

* **Purpose:** Represent objects that can modify a Fighter's attributes or states.
* **Practical use:**

    * Contains name, description, and list of `Effects`
    * Can be used on self or another Fighter
    * Supports consumables, equipment, or strategic items
* **Implementation flow:**

    1. Create item with desired effects.
    2. Add to Fighter’s inventory.
    3. Call `useItem(item, target)` during turn.
* **Example:**

  ```kotlin
  val sword = Item("Sword of Strength", "Increases strength", listOf(AttributeBoostEffect("strength", 5)))
  warrior.inventory.addItem(sword)
  warrior.useItem(sword, warrior)
  ```

---

### 8. **[Inventory](src/main/kotlin/com/lucasalfare/flbattle/Inventory.kt)**

* **Purpose:** Organize and manage a Fighter’s items in a centralized way.
* **Practical use:**

    * Allows adding, removing, and listing items
    * Facilitates strategic item usage during battle
* **Implementation flow:**

    1. Create `Inventory` inside `Fighter`.
    2. Add items to inventory.
    3. List or use items as needed per turn.
* **Example:**

  ```kotlin
  val inventory = Inventory()
  inventory.addItem(potion)
  inventory.listItems()
  ```

---

## Combat Flow Suggestion

1. Turn starts → active Fighter decides action
2. Apply passive or active items (`Effect.apply()`)
3. Attack calculated via `Rule` (`calculate`)
4. Validators applied for consistency check
5. Damage or effect applied (`Fighter.receiveDamage()` or `Effect`)
6. Turn ends → next Fighter
7. Repeat until a winner is determined

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