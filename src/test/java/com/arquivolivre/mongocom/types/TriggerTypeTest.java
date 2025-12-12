package com.arquivolivre.mongocom.types;

import static org.junit.Assert.*;
import org.junit.Test;

/** Unit tests for TriggerType enum. */
public class TriggerTypeTest {

  @Test
  public void testTriggerTypeValues() {
    TriggerType[] types = TriggerType.values();
    assertEquals(2, types.length);
    assertEquals(TriggerType.BEFORE, types[0]);
    assertEquals(TriggerType.AFTER, types[1]);
  }

  @Test
  public void testTriggerTypeValueOf() {
    assertEquals(TriggerType.BEFORE, TriggerType.valueOf("BEFORE"));
    assertEquals(TriggerType.AFTER, TriggerType.valueOf("AFTER"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTriggerTypeValueOfInvalid() {
    TriggerType.valueOf("INVALID_TYPE");
  }

  @Test
  public void testTriggerTypeEnumEquality() {
    TriggerType type1 = TriggerType.BEFORE;
    TriggerType type2 = TriggerType.BEFORE;
    assertEquals(type1, type2);
    assertSame(type1, type2);
  }

  @Test
  public void testTriggerTypeEnumInequality() {
    TriggerType type1 = TriggerType.BEFORE;
    TriggerType type2 = TriggerType.AFTER;
    assertNotEquals(type1, type2);
  }
}