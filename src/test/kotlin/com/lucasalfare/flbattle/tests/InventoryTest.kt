package com.lucasalfare.flbattle.tests

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InventoryTest {

  private fun createItem(name: String, description: String): com.lucasalfare.flbattle.Item {
    return _root_ide_package_.com.lucasalfare.flbattle.Item(name, description, emptyList())
  }

  @Test
  fun `addItem adds item to inventory`() {
    val inventory = _root_ide_package_.com.lucasalfare.flbattle.Inventory()
    val item = createItem("Potion", "Heals HP")
    inventory.addItem(item)
    assertTrue(inventory.getItems().contains(item))
  }

  @Test
  fun `removeItem removes item from inventory`() {
    val inventory = _root_ide_package_.com.lucasalfare.flbattle.Inventory()
    val item = createItem("Potion", "Heals HP")
    inventory.addItem(item)
    inventory.removeItem(item)
    assertFalse(inventory.getItems().contains(item))
  }

  @Test
  fun `removeItem does nothing if item not present`() {
    val inventory = _root_ide_package_.com.lucasalfare.flbattle.Inventory()
    val item = createItem("Potion", "Heals HP")
    inventory.removeItem(item)
    assertTrue(inventory.getItems().isEmpty())
  }

  @Test
  fun `getItems returns immutable view`() {
    val inventory = _root_ide_package_.com.lucasalfare.flbattle.Inventory()
    val item = createItem("Potion", "Heals HP")
    inventory.addItem(item)
    val items = inventory.getItems()
    assertEquals(1, items.size)
    assertEquals("Potion", items[0].name)
  }

  @Test
  fun `listItems works with empty inventory`() {
    val inventory = _root_ide_package_.com.lucasalfare.flbattle.Inventory()
    inventory.listItems()

    assertTrue(inventory.getItems().isEmpty())
  }
}