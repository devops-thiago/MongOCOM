package com.arquivolivre.mongocom.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("QueryPrototype Tests")
class QueryPrototypeTest {

  @Test
  @DisplayName("Should have static select method")
  void testSelectMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.select("field1", "field2");
        });
  }

  @Test
  @DisplayName("Should have static from method")
  void testFromMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.from(String.class);
        });
  }

  @Test
  @DisplayName("Should have static where method")
  void testWhereMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.where("field");
        });
  }

  @Test
  @DisplayName("Should have static equalsTo method")
  void testEqualsToMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.equalsTo("value");
        });
  }

  @Test
  @DisplayName("Should have static notEqualsTo method")
  void testNotEqualsToMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.notEqualsTo("value");
        });
  }

  @Test
  @DisplayName("Should have static greaterThan method")
  void testGreaterThanMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.greaterThan(10);
        });
  }

  @Test
  @DisplayName("Should have static lessThan method")
  void testLessThanMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.lessThan(10);
        });
  }

  @Test
  @DisplayName("Should have static greaterThanOrEqualTo method")
  void testGreaterThanOrEqualToMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.greaterThanOrEqualTo(10);
        });
  }

  @Test
  @DisplayName("Should have static lessThanOrEqualTo method")
  void testLessThanOrEqualToMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.lessThanOrEqualTo(10);
        });
  }

  @Test
  @DisplayName("Should have static and method")
  void testAndMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.and("field");
        });
  }

  @Test
  @DisplayName("Should have static or method")
  void testOrMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.or("field");
        });
  }

  @Test
  @DisplayName("Should have static exists method")
  void testExistsMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.exists();
        });
  }

  @Test
  @DisplayName("Should have static notExists method")
  void testNotExistsMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.notExists();
        });
  }

  @Test
  @DisplayName("Should have static in method")
  void testInMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.in("value1", "value2", "value3");
        });
  }

  @Test
  @DisplayName("Should have static notIn method")
  void testNotInMethod() {
    // Test that the method exists and can be called
    assertDoesNotThrow(
        () -> {
          QueryPrototype.notIn("value1", "value2", "value3");
        });
  }

  @Test
  @DisplayName("Should allow chaining query methods")
  void testMethodChaining() {
    // Test that all methods can be called in sequence (fluent API style)
    assertDoesNotThrow(
        () -> {
          QueryPrototype.select("name", "age");
          QueryPrototype.from(String.class);
          QueryPrototype.where("name");
          QueryPrototype.equalsTo("John");
          QueryPrototype.and("age");
          QueryPrototype.greaterThan(18);
        });
  }
}
