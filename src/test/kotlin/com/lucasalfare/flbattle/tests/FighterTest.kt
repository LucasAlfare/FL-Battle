package com.lucasalfare.flbattle.tests

import com.lucasalfare.flbattle.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FighterTest {

  private class FixedRule(private val damage: Int) : Rule {
    override fun calculate(attacker: Fighter, defender: Fighter): Int = damage
  }

  private class AlwaysValidator(private val allow: Boolean) : Validator {
    override fun validate(attacker: Fighter, defender: Fighter, damage: Int) = allow
  }

  @Test
  fun `initial hp comes from attributes`() {
    val attrs = Attributes(mutableMapOf("hp" to 100))
    val fighter = Fighter("Hero", attrs, emptyList())
    assertEquals(100, fighter.hp)
  }

  @Test
  fun `isAlive returns true when hp positive`() {
    val attrs = Attributes(mutableMapOf("hp" to 10))
    val fighter = Fighter("Hero", attrs, emptyList())
    assertTrue(fighter.isAlive())
  }

  @Test
  fun `isAlive returns false when hp is zero`() {
    val attrs = Attributes(mutableMapOf("hp" to 0))
    val fighter = Fighter("Hero", attrs, emptyList())
    assertFalse(fighter.isAlive())
  }

  @Test
  fun `isAlive returns false when hp negative`() {
    val attrs = Attributes(mutableMapOf("hp" to -5))
    val fighter = Fighter("Hero", attrs, emptyList())
    assertFalse(fighter.isAlive())
  }

  @Test
  fun `receiveDamage decreases hp`() {
    val attrs = Attributes(mutableMapOf("hp" to 50))
    val hero = Fighter("Hero", attrs, emptyList())
    val enemy = Fighter("Enemy", Attributes(mutableMapOf("hp" to 50)), emptyList())

    hero.receiveDamage(20, enemy)
    assertEquals(30, hero.hp)
  }

  @Test
  fun `attack applies rule damage when validator allows`() {
    val attacker = Fighter("Hero", Attributes(mutableMapOf("hp" to 100)), listOf(FixedRule(25)))
    val defender = Fighter("Enemy", Attributes(mutableMapOf("hp" to 80)), emptyList())
    attacker.attack(defender, listOf(AlwaysValidator(true)))
    assertEquals(55, defender.hp)
  }

  @Test
  fun `attack does nothing when validator blocks`() {
    val attacker = Fighter("Hero", Attributes(mutableMapOf("hp" to 100)), listOf(FixedRule(25)))
    val defender = Fighter("Enemy", Attributes(mutableMapOf("hp" to 80)), emptyList())
    attacker.attack(defender, listOf(AlwaysValidator(false)))
    assertEquals(80, defender.hp)
  }

  @Test
  fun `attack applies multiple rules cumulatively`() {
    val attacker = Fighter("Hero", Attributes(mutableMapOf("hp" to 100)), listOf(FixedRule(10), FixedRule(15)))
    val defender = Fighter("Enemy", Attributes(mutableMapOf("hp" to 50)), emptyList())
    attacker.attack(defender, listOf(AlwaysValidator(true)))
    assertEquals(25, defender.hp)
  }

  @Test
  fun `useItem does nothing if item not in inventory`() {
    val fighter = Fighter("Hero", Attributes(mutableMapOf("hp" to 100)), emptyList())
    val target = Fighter("Enemy", Attributes(mutableMapOf("hp" to 100)), emptyList())
    val item = Item("Potion", "Heals 10 HP", listOf(object : Effect {
      override fun apply(target: Fighter) {
        target.receiveDamage(-10, fighter)
      }
    }))
    fighter.useItem(item, target)
    assertEquals(100, target.hp) // no effect applied
  }

  @Test
  fun `useItem applies item effects if present in inventory`() {
    val fighter = Fighter("Hero", Attributes(mutableMapOf("hp" to 100)), emptyList())
    val target = Fighter("Enemy", Attributes(mutableMapOf("hp" to 50)), emptyList())
    val heal = Item("Potion", "Heals 20 HP", listOf(object : Effect {
      override fun apply(target: Fighter) {
        target.receiveDamage(-20, fighter)
      }
    }))
    fighter.inventory.addItem(heal)

    fighter.useItem(heal, target)
    assertEquals(70, target.hp)
  }
}