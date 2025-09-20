package com.lucasalfare.flbattle.tests

import kotlin.test.Test
import kotlin.test.assertEquals

class AttributesTest {

  @Test
  fun `get returns 0 for nonexistent attribute`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    assertEquals(0, attrs.get("hp"))
  }

  @Test
  fun `set defines attribute value`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.set("hp", 100)
    assertEquals(100, attrs.get("hp"))
  }

  @Test
  fun `set overwrites existing attribute`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.set("hp", 100)
    attrs.set("hp", 50)
    assertEquals(50, attrs.get("hp"))
  }

  @Test
  fun `add creates attribute if nonexistent`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.add("strength", 10)
    assertEquals(10, attrs.get("strength"))
  }

  @Test
  fun `add increases existing attribute`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.set("strength", 5)
    attrs.add("strength", 7)
    assertEquals(12, attrs.get("strength"))
  }

  @Test
  fun `add works with negative delta`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.set("defense", 20)
    attrs.add("defense", -5)
    assertEquals(15, attrs.get("defense"))
  }

  @Test
  fun `add negative delta creating new attribute`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.add("magic_resist", -10)
    assertEquals(-10, attrs.get("magic_resist"))
  }

  @Test
  fun `get multiple attributes independently`() {
    val attrs = _root_ide_package_.com.lucasalfare.flbattle.Attributes()
    attrs.set("hp", 100)
    attrs.set("strength", 30)
    assertEquals(100, attrs.get("hp"))
    assertEquals(30, attrs.get("strength"))
    assertEquals(0, attrs.get("defense"))
  }
}