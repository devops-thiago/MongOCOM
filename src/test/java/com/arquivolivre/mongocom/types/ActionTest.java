package com.arquivolivre.mongocom.types;

import static org.junit.Assert.*;
import org.junit.Test;

/** Unit tests for Action enum. */
public class ActionTest {

  @Test
  public void testActionValues() {
    Action[] actions = Action.values();
    assertEquals(3, actions.length);
    assertEquals(Action.ON_UPDATE, actions[0]);
    assertEquals(Action.ON_INSERT, actions[1]);
    assertEquals(Action.ON_REMOVE, actions[2]);
  }

  @Test
  public void testActionValueOf() {
    assertEquals(Action.ON_UPDATE, Action.valueOf("ON_UPDATE"));
    assertEquals(Action.ON_INSERT, Action.valueOf("ON_INSERT"));
    assertEquals(Action.ON_REMOVE, Action.valueOf("ON_REMOVE"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testActionValueOfInvalid() {
    Action.valueOf("INVALID_ACTION");
  }

  @Test
  public void testActionEnumEquality() {
    Action action1 = Action.ON_UPDATE;
    Action action2 = Action.ON_UPDATE;
    assertEquals(action1, action2);
    assertSame(action1, action2);
  }

  @Test
  public void testActionEnumInequality() {
    Action action1 = Action.ON_UPDATE;
    Action action2 = Action.ON_INSERT;
    assertNotEquals(action1, action2);
  }
}