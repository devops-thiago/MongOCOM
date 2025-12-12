package com.arquivolivre.mongocom.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.util.Date;

/** Unit tests for DateGenerator. */
public class DateGeneratorTest {

  private DateGenerator generator;

  @Before
  public void setUp() {
    generator = new DateGenerator();
  }

  @Test
  public void testGenerateValue() {
    Date result = generator.generateValue(Object.class, null);

    assertNotNull(result);
    assertTrue(result instanceof Date);
  }

  @Test
  public void testGenerateValueReturnsCurrentDate() {
    long beforeTime = System.currentTimeMillis();
    Date result = generator.generateValue(Object.class, null);
    long afterTime = System.currentTimeMillis();

    assertNotNull(result);
    assertTrue(result.getTime() >= beforeTime);
    assertTrue(result.getTime() <= afterTime);
  }

  @Test
  public void testGenerateValueWithDifferentClasses() {
    Date result1 = generator.generateValue(String.class, null);
    Date result2 = generator.generateValue(Integer.class, null);

    assertNotNull(result1);
    assertNotNull(result2);
    // Both should be close in time
    assertTrue(Math.abs(result1.getTime() - result2.getTime()) < 1000);
  }

  @Test
  public void testGenerateValueMultipleCalls() {
    Date result1 = generator.generateValue(Object.class, null);
    try {
      Thread.sleep(10); // Small delay to ensure different timestamps
    } catch (InterruptedException e) {
      // Ignore
    }
    Date result2 = generator.generateValue(Object.class, null);

    assertNotNull(result1);
    assertNotNull(result2);
    assertTrue(result2.getTime() >= result1.getTime());
  }

  @Test
  public void testImplementsGeneratorInterface() {
    assertTrue(generator instanceof Generator);
  }
}