package com.arquivolivre.mongocom.types;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for type enums and constants.
 *
 * <p>Tests enum values, constants, and valueOf/values methods.
 */
public class TypesTest {

  // Action enum tests
  @Test
  public void testActionEnumValues() {
    Action[] actions = Action.values();
    assertEquals(3, actions.length);
  }

  @Test
  public void testActionOnUpdate() {
    Action action = Action.ON_UPDATE;
    assertNotNull(action);
    assertEquals("ON_UPDATE", action.name());
  }

  @Test
  public void testActionOnInsert() {
    Action action = Action.ON_INSERT;
    assertNotNull(action);
    assertEquals("ON_INSERT", action.name());
  }

  @Test
  public void testActionOnRemove() {
    Action action = Action.ON_REMOVE;
    assertNotNull(action);
    assertEquals("ON_REMOVE", action.name());
  }

  @Test
  public void testActionValueOf() {
    assertEquals(Action.ON_UPDATE, Action.valueOf("ON_UPDATE"));
    assertEquals(Action.ON_INSERT, Action.valueOf("ON_INSERT"));
    assertEquals(Action.ON_REMOVE, Action.valueOf("ON_REMOVE"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testActionValueOfInvalid() {
    Action.valueOf("INVALID");
  }

  @Test(expected = NullPointerException.class)
  public void testActionValueOfNull() {
    Action.valueOf(null);
  }

  @Test
  public void testActionToString() {
    assertEquals("ON_UPDATE", Action.ON_UPDATE.toString());
    assertEquals("ON_INSERT", Action.ON_INSERT.toString());
    assertEquals("ON_REMOVE", Action.ON_REMOVE.toString());
  }

  @Test
  public void testActionOrdinal() {
    assertEquals(0, Action.ON_UPDATE.ordinal());
    assertEquals(1, Action.ON_INSERT.ordinal());
    assertEquals(2, Action.ON_REMOVE.ordinal());
  }

  @Test
  public void testActionEquality() {
    Action action1 = Action.ON_UPDATE;
    Action action2 = Action.valueOf("ON_UPDATE");
    assertSame(action1, action2);
    assertEquals(action1, action2);
  }

  @Test
  public void testActionInequality() {
    assertNotEquals(Action.ON_UPDATE, Action.ON_INSERT);
    assertNotEquals(Action.ON_INSERT, Action.ON_REMOVE);
    assertNotEquals(Action.ON_UPDATE, Action.ON_REMOVE);
  }

  @Test
  public void testActionCompareTo() {
    assertTrue(Action.ON_UPDATE.compareTo(Action.ON_INSERT) < 0);
    assertTrue(Action.ON_INSERT.compareTo(Action.ON_REMOVE) < 0);
    assertTrue(Action.ON_REMOVE.compareTo(Action.ON_UPDATE) > 0);
    assertEquals(0, Action.ON_UPDATE.compareTo(Action.ON_UPDATE));
  }

  // TriggerType enum tests
  @Test
  public void testTriggerTypeEnumValues() {
    TriggerType[] types = TriggerType.values();
    assertEquals(2, types.length);
  }

  @Test
  public void testTriggerTypeBefore() {
    TriggerType type = TriggerType.BEFORE;
    assertNotNull(type);
    assertEquals("BEFORE", type.name());
  }

  @Test
  public void testTriggerTypeAfter() {
    TriggerType type = TriggerType.AFTER;
    assertNotNull(type);
    assertEquals("AFTER", type.name());
  }

  @Test
  public void testTriggerTypeValueOf() {
    assertEquals(TriggerType.BEFORE, TriggerType.valueOf("BEFORE"));
    assertEquals(TriggerType.AFTER, TriggerType.valueOf("AFTER"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTriggerTypeValueOfInvalid() {
    TriggerType.valueOf("INVALID");
  }

  @Test(expected = NullPointerException.class)
  public void testTriggerTypeValueOfNull() {
    TriggerType.valueOf(null);
  }

  @Test
  public void testTriggerTypeToString() {
    assertEquals("BEFORE", TriggerType.BEFORE.toString());
    assertEquals("AFTER", TriggerType.AFTER.toString());
  }

  @Test
  public void testTriggerTypeOrdinal() {
    assertEquals(0, TriggerType.BEFORE.ordinal());
    assertEquals(1, TriggerType.AFTER.ordinal());
  }

  @Test
  public void testTriggerTypeEquality() {
    TriggerType type1 = TriggerType.BEFORE;
    TriggerType type2 = TriggerType.valueOf("BEFORE");
    assertSame(type1, type2);
    assertEquals(type1, type2);
  }

  @Test
  public void testTriggerTypeInequality() {
    assertNotEquals(TriggerType.BEFORE, TriggerType.AFTER);
  }

  @Test
  public void testTriggerTypeCompareTo() {
    assertTrue(TriggerType.BEFORE.compareTo(TriggerType.AFTER) < 0);
    assertTrue(TriggerType.AFTER.compareTo(TriggerType.BEFORE) > 0);
    assertEquals(0, TriggerType.BEFORE.compareTo(TriggerType.BEFORE));
  }

  // IndexType constants tests
  @Test
  public void testIndexTypeAscending() {
    assertEquals(1, IndexType.INDEX_ASCENDING);
  }

  @Test
  public void testIndexTypeDescending() {
    assertEquals(-1, IndexType.INDEX_DESCENDING);
  }

  @Test
  public void testIndexTypeText() {
    assertEquals("text", IndexType.INDEX_TEXT);
    assertNotNull(IndexType.INDEX_TEXT);
  }

  @Test
  public void testIndexTypeHashed() {
    assertEquals("hashed", IndexType.INDEX_HASHED);
    assertNotNull(IndexType.INDEX_HASHED);
  }

  @Test
  public void testIndexTypeAscendingDescendingOpposite() {
    assertEquals(-IndexType.INDEX_ASCENDING, IndexType.INDEX_DESCENDING);
    assertEquals(-IndexType.INDEX_DESCENDING, IndexType.INDEX_ASCENDING);
  }

  @Test
  public void testIndexTypeTextNotEmpty() {
    assertFalse(IndexType.INDEX_TEXT.isEmpty());
    assertTrue(IndexType.INDEX_TEXT.length() > 0);
  }

  @Test
  public void testIndexTypeHashedNotEmpty() {
    assertFalse(IndexType.INDEX_HASHED.isEmpty());
    assertTrue(IndexType.INDEX_HASHED.length() > 0);
  }

  @Test
  public void testIndexTypeTextAndHashedDifferent() {
    assertNotEquals(IndexType.INDEX_TEXT, IndexType.INDEX_HASHED);
  }

  @Test
  public void testIndexTypeCanInstantiate() {
    // IndexType is not meant to be instantiated, but Java allows it
    // This test verifies the class can be loaded
    IndexType indexType = new IndexType();
    assertNotNull(indexType);
  }

  // Integration tests - using enums with constants
  @Test
  public void testActionWithIndexType() {
    // Verify enums and constants can be used together
    Action action = Action.ON_INSERT;
    int indexType = IndexType.INDEX_ASCENDING;

    assertNotNull(action);
    assertEquals(1, indexType);
  }

  @Test
  public void testTriggerTypeWithAction() {
    // Verify different enums can be used together
    TriggerType triggerType = TriggerType.BEFORE;
    Action action = Action.ON_UPDATE;

    assertNotNull(triggerType);
    assertNotNull(action);
    assertEquals("BEFORE", triggerType.name());
    assertEquals("ON_UPDATE", action.name());
  }

  @Test
  public void testAllTypesInSwitchStatement() {
    // Test that enums work in switch statements
    Action action = Action.ON_INSERT;
    String result;

    switch (action) {
      case ON_UPDATE:
        result = "update";
        break;
      case ON_INSERT:
        result = "insert";
        break;
      case ON_REMOVE:
        result = "remove";
        break;
      default:
        result = "unknown";
    }

    assertEquals("insert", result);
  }

  @Test
  public void testTriggerTypeInSwitchStatement() {
    TriggerType type = TriggerType.AFTER;
    String result;

    switch (type) {
      case BEFORE:
        result = "before";
        break;
      case AFTER:
        result = "after";
        break;
      default:
        result = "unknown";
    }

    assertEquals("after", result);
  }
}
