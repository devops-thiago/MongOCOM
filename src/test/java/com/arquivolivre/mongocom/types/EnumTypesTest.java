package com.arquivolivre.mongocom.types;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Enum Types Tests")
class EnumTypesTest {

  @Test
  @DisplayName("Action enum should have expected values")
  void testActionEnumValues() {
    Action[] actions = Action.values();

    assertNotNull(actions);
    assertEquals(3, actions.length);

    // Test specific enum values exist based on actual implementation
    assertNotNull(Action.valueOf("ON_UPDATE"));
    assertNotNull(Action.valueOf("ON_INSERT"));
    assertNotNull(Action.valueOf("ON_REMOVE"));
  }

  @Test
  @DisplayName("Action enum should be serializable")
  void testActionEnumSerializable() {
    for (Action action : Action.values()) {
      assertInstanceOf(java.io.Serializable.class, action);
    }
  }

  @Test
  @DisplayName("Action enum should have consistent toString")
  void testActionEnumToString() {
    assertEquals("ON_UPDATE", Action.ON_UPDATE.toString());
    assertEquals("ON_INSERT", Action.ON_INSERT.toString());
    assertEquals("ON_REMOVE", Action.ON_REMOVE.toString());
  }

  @Test
  @DisplayName("TriggerType enum should have expected values")
  void testTriggerTypeEnumValues() {
    TriggerType[] triggerTypes = TriggerType.values();

    assertNotNull(triggerTypes);
    assertEquals(2, triggerTypes.length);

    // Test specific enum values exist based on actual implementation
    assertNotNull(TriggerType.valueOf("BEFORE"));
    assertNotNull(TriggerType.valueOf("AFTER"));
  }

  @Test
  @DisplayName("TriggerType enum should be serializable")
  void testTriggerTypeEnumSerializable() {
    for (TriggerType triggerType : TriggerType.values()) {
      assertInstanceOf(java.io.Serializable.class, triggerType);
    }
  }

  @Test
  @DisplayName("TriggerType enum should have consistent toString")
  void testTriggerTypeEnumToString() {
    assertEquals("BEFORE", TriggerType.BEFORE.toString());
    assertEquals("AFTER", TriggerType.AFTER.toString());
  }

  @Test
  @DisplayName("IndexType should have expected constants")
  void testIndexTypeConstants() {
    // IndexType is a class with static constants, not an enum
    assertEquals(1, IndexType.INDEX_ASCENDING);
    assertEquals(-1, IndexType.INDEX_DESCENDING);
    assertEquals("text", IndexType.INDEX_TEXT);
    assertEquals("hashed", IndexType.INDEX_HASHED);
  }

  @Test
  @DisplayName("Action enum should have correct ordinal values")
  void testActionEnumOrdinals() {
    assertEquals(0, Action.ON_UPDATE.ordinal());
    assertEquals(1, Action.ON_INSERT.ordinal());
    assertEquals(2, Action.ON_REMOVE.ordinal());
  }

  @Test
  @DisplayName("TriggerType enum should have correct ordinal values")
  void testTriggerTypeEnumOrdinals() {
    assertEquals(0, TriggerType.BEFORE.ordinal());
    assertEquals(1, TriggerType.AFTER.ordinal());
  }

  @Test
  @DisplayName("Action enum should handle valueOf correctly")
  void testActionValueOf() {
    assertEquals(Action.ON_UPDATE, Action.valueOf("ON_UPDATE"));
    assertEquals(Action.ON_INSERT, Action.valueOf("ON_INSERT"));
    assertEquals(Action.ON_REMOVE, Action.valueOf("ON_REMOVE"));

    // Test invalid value throws exception
    assertThrows(IllegalArgumentException.class, () -> Action.valueOf("INVALID"));
  }

  @Test
  @DisplayName("TriggerType enum should handle valueOf correctly")
  void testTriggerTypeValueOf() {
    assertEquals(TriggerType.BEFORE, TriggerType.valueOf("BEFORE"));
    assertEquals(TriggerType.AFTER, TriggerType.valueOf("AFTER"));

    // Test invalid value throws exception
    assertThrows(IllegalArgumentException.class, () -> TriggerType.valueOf("INVALID"));
  }

  @Test
  @DisplayName("Action enum should have proper name() method")
  void testActionName() {
    assertEquals("ON_UPDATE", Action.ON_UPDATE.name());
    assertEquals("ON_INSERT", Action.ON_INSERT.name());
    assertEquals("ON_REMOVE", Action.ON_REMOVE.name());
  }

  @Test
  @DisplayName("TriggerType enum should have proper name() method")
  void testTriggerTypeName() {
    assertEquals("BEFORE", TriggerType.BEFORE.name());
    assertEquals("AFTER", TriggerType.AFTER.name());
  }

  @Test
  @DisplayName("Enums should be comparable")
  void testEnumComparison() {
    assertTrue(Action.ON_UPDATE.compareTo(Action.ON_INSERT) < 0);
    assertTrue(Action.ON_INSERT.compareTo(Action.ON_REMOVE) < 0);

    assertTrue(TriggerType.BEFORE.compareTo(TriggerType.AFTER) < 0);
    assertEquals(0, TriggerType.BEFORE.compareTo(TriggerType.BEFORE));
  }

  @Test
  @DisplayName("Enums should have proper equals implementation")
  void testEnumEquals() {
    assertEquals(Action.ON_UPDATE, Action.ON_UPDATE);
    assertNotEquals(Action.ON_UPDATE, Action.ON_INSERT);

    assertEquals(TriggerType.BEFORE, TriggerType.BEFORE);
    assertNotEquals(TriggerType.BEFORE, TriggerType.AFTER);
  }

  @Test
  @DisplayName("Enums should have proper hashCode implementation")
  void testEnumHashCode() {
    assertEquals(Action.ON_UPDATE.hashCode(), Action.ON_UPDATE.hashCode());
    assertNotEquals(Action.ON_UPDATE.hashCode(), Action.ON_INSERT.hashCode());

    assertEquals(TriggerType.BEFORE.hashCode(), TriggerType.BEFORE.hashCode());
    assertNotEquals(TriggerType.BEFORE.hashCode(), TriggerType.AFTER.hashCode());
  }

  @Test
  @DisplayName("IndexType constants should be immutable")
  void testIndexTypeImmutability() {
    // Test that the constants are final and cannot be changed
    // We can't directly test final modifier, but we can test the values remain consistent
    assertEquals(1, IndexType.INDEX_ASCENDING);
    assertEquals(-1, IndexType.INDEX_DESCENDING);
    assertEquals("text", IndexType.INDEX_TEXT);
    assertEquals("hashed", IndexType.INDEX_HASHED);

    // Test that string constants are interned
    assertSame("text", IndexType.INDEX_TEXT);
    assertSame("hashed", IndexType.INDEX_HASHED);
  }
}
