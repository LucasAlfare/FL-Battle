package com.lucasalfare.flbattle.tests

import com.lucasalfare.flbattle.*
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.*

class BattleTest {

  private fun fighter(name: String, hp: Int, rules: List<Rule> = emptyList()): Fighter {
    return Fighter(name, Attributes(mutableMapOf("hp" to hp)), rules)
  }

  private class FixedRule(private val damage: Int) : Rule {
    override fun calculate(attacker: Fighter, defender: Fighter) = damage
  }

  private class AlwaysValidator(private val allow: Boolean) : Validator {
    override fun validate(attacker: Fighter, defender: Fighter, damage: Int) = allow
  }

  @Test
  fun `begin starts at TURN_START if both alive`() {
    val b = Battle(fighter("A", 100), fighter("B", 100))
    b.begin()
    assertEquals(Battle.Phase.TURN_START, b.phase)
  }

  @Test
  fun `begin starts at FINISHED if one dead`() {
    val b = Battle(fighter("A", 0), fighter("B", 100))
    b.begin()
    assertEquals(Battle.Phase.FINISHED, b.phase)
  }

  @Test
  fun `advancePhase follows correct transitions`() {
    val b = Battle(fighter("A", 100), fighter("B", 100))
    b.begin()
    assertEquals(Battle.Phase.TURN_START, b.phase)

    assertEquals(Battle.Phase.PRE_ITEM, b.advancePhase())
    assertEquals(Battle.Phase.ACTION, b.advancePhase())
    assertEquals(Battle.Phase.POST_ACTION, b.advancePhase())
    assertEquals(Battle.Phase.TURN_END, b.advancePhase())
  }

  @Test
  fun `advancePhase swaps attacker and defender after TURN_END`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)

    b.begin()
    b.advancePhase() // TURN_START -> PRE_ITEM
    b.advancePhase() // PRE_ITEM -> ACTION
    b.advancePhase() // ACTION -> POST_ACTION
    b.advancePhase() // POST_ACTION -> TURN_END
    b.advancePhase() // TURN_END -> TURN_START + swap

    assertEquals(f2, b.currentAttacker)
    assertEquals(f1, b.currentDefender)
  }

  @Test
  fun `advancePhase ends battle if defender dies at TURN_END`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 0)
    val b = Battle(f1, f2)
    b.begin()
    assertEquals(Battle.Phase.FINISHED, b.phase)
  }

  @Test
  fun `callbacks fire on phase change`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    var called = false
    b.on(Battle.Phase.ACTION) { called = true }
    b.begin()
    b.advancePhase() // PRE_ITEM
    b.advancePhase() // ACTION
    assertTrue(called)
  }

  @Test
  fun `availableItems returns attacker items`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    val potion = Item("Potion", "Heals", emptyList())
    f1.inventory.addItem(potion)
    b.begin()
    assertTrue(b.availableItems().contains(potion))
  }

  @Test
  fun `useItem only allowed in PRE_ITEM phase`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    val potion = Item("Potion", "Heals", emptyList())
    f1.inventory.addItem(potion)
    b.begin()
    assertThrows(IllegalStateException::class.java) {
      b.useItem(potion, f1)
    }
  }

  @Test
  fun `useItem applies effect in PRE_ITEM`() {
    val f1 = fighter("A", 50)
    val f2 = fighter("B", 100)
    val heal = Item("Potion", "Heals 20", listOf(object : Effect {
      override fun apply(target: Fighter) {
        target.receiveDamage(-20, f1)
      }
    }))
    f1.inventory.addItem(heal)
    val b = Battle(f1, f2)
    b.begin()
    b.advancePhase() // PRE_ITEM
    b.useItem(heal, f1)
    assertEquals(70, f1.hp)
  }

  @Test
  fun `useItem can end battle if target dies`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 10)
    val kill = Item("Bomb", "Deals 20", listOf(object : Effect {
      override fun apply(target: Fighter) {
        target.receiveDamage(20, f1)
      }
    }))
    f1.inventory.addItem(kill)
    val b = Battle(f1, f2)
    b.begin()
    b.advancePhase() // PRE_ITEM
    b.useItem(kill, f2)
    assertTrue(b.isFinished())
    assertEquals(f1, b.winner())
  }

  @Test
  fun `attack only allowed in ACTION phase`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    b.begin()
    assertThrows(IllegalStateException::class.java) { b.attack() }
  }

  @Test
  fun `attack applies damage and continues if defender alive`() {
    val f1 = fighter("A", 100, listOf(FixedRule(30)))
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    b.begin()
    b.advancePhase() // PRE_ITEM
    b.advancePhase() // ACTION
    b.attack()
    assertEquals(70, f2.hp)
    assertFalse(b.isFinished())
  }

  @Test
  fun `attack finishes battle if defender dies`() {
    val f1 = fighter("A", 100, listOf(FixedRule(200)))
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    b.begin()
    b.advancePhase() // PRE_ITEM
    b.advancePhase() // ACTION
    b.attack()
    assertTrue(b.isFinished())
    assertEquals(f1, b.winner())
  }

  @Test
  fun `winner returns correct fighter`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 0)
    val b = Battle(f1, f2)
    b.begin()
    assertEquals(f1, b.winner())
  }

  @Test
  fun `winner returns null if both alive`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    b.begin()
    assertNull(b.winner())
  }

  @Test
  fun `forceFinish ends battle immediately`() {
    val f1 = fighter("A", 100)
    val f2 = fighter("B", 100)
    val b = Battle(f1, f2)
    b.begin()
    b.forceFinish()
    assertTrue(b.isFinished())
  }
}