package com.arquivolivre.mongocom.utils;

import static org.junit.Assert.*;
import org.junit.Test;

/** Unit tests for QueryPrototype. */
public class QueryPrototypeTest {

  @Test
  public void testSelectMethod() {
    // Test that method can be called without exception
    QueryPrototype.select("field1", "field2");
    QueryPrototype.select("field1");
    QueryPrototype.select();
  }

  @Test
  public void testFromMethod() {
    QueryPrototype.from(Object.class);
    QueryPrototype.from(String.class);
  }

  @Test
  public void testWhereMethod() {
    QueryPrototype.where("field");
    QueryPrototype.where("anotherField");
  }

  @Test
  public void testEqualsToMethod() {
    QueryPrototype.equalsTo("value");
    QueryPrototype.equalsTo(123);
    QueryPrototype.equalsTo(null);
  }

  @Test
  public void testNotEqualsToMethod() {
    QueryPrototype.notEqualsTo("value");
    QueryPrototype.notEqualsTo(456);
  }

  @Test
  public void testGreaterThanMethod() {
    QueryPrototype.greaterThan(100);
    QueryPrototype.greaterThan("value");
  }

  @Test
  public void testLessThanMethod() {
    QueryPrototype.lessThan(50);
    QueryPrototype.lessThan("value");
  }

  @Test
  public void testGreaterThanOrEqualToMethod() {
    QueryPrototype.greaterThanOrEqualTo(100);
    QueryPrototype.greaterThanOrEqualTo("value");
  }

  @Test
  public void testLessThanOrEqualToMethod() {
    QueryPrototype.lessThanOrEqualTo(50);
    QueryPrototype.lessThanOrEqualTo("value");
  }

  @Test
  public void testAndMethod() {
    QueryPrototype.and("field1");
    QueryPrototype.and("field2");
  }

  @Test
  public void testOrMethod() {
    QueryPrototype.or("field1");
    QueryPrototype.or("field2");
  }

  @Test
  public void testExistsMethod() {
    QueryPrototype.exists();
  }

  @Test
  public void testNotExistsMethod() {
    QueryPrototype.notExists();
  }

  @Test
  public void testInMethod() {
    QueryPrototype.in("value1", "value2", "value3");
    QueryPrototype.in(1, 2, 3);
    QueryPrototype.in();
  }

  @Test
  public void testNotInMethod() {
    QueryPrototype.notIn("value1", "value2");
    QueryPrototype.notIn(1, 2, 3, 4);
    QueryPrototype.notIn();
  }

  @Test
  public void testQueryPrototypeInstantiation() {
    QueryPrototype prototype = new QueryPrototype();
    assertNotNull(prototype);
  }

  @Test
  public void testChainedMethodCalls() {
    // Test that methods can be called in sequence without errors
    QueryPrototype.select("field1", "field2");
    QueryPrototype.from(Object.class);
    QueryPrototype.where("field1");
    QueryPrototype.equalsTo("value");
    QueryPrototype.and("field2");
    QueryPrototype.greaterThan(100);
  }
}