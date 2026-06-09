/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arquivolivre.mongocom.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Date;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Generator implementations.
 *
 * <p>Tests DateGenerator and IntegerGenerator functionality.
 */
public class GeneratorsTest {

  private MongoDatabase mockDatabase;
  private MongoCollection<Document> mockCollection;

  private static class TestEntity {}

  @Before
  public void setUp() {
    mockDatabase = mock(MongoDatabase.class);
    mockCollection = mock(MongoCollection.class);
  }

  // ==================== DateGenerator Tests ====================

  @Test
  public void testDateGenerator_GenerateValue_ShouldReturnDate() {
    final DateGenerator generator = new DateGenerator();
    final Date result = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull("Generated date should not be null", result);
    assertTrue("Generated date should be Date instance", result instanceof Date);
  }

  @Test
  public void testDateGenerator_GenerateValue_ShouldReturnCurrentTime() {
    final DateGenerator generator = new DateGenerator();
    final long before = System.currentTimeMillis();
    final Date result = generator.generateValue(TestEntity.class, mockDatabase);
    final long after = System.currentTimeMillis();

    assertNotNull(result);
    assertTrue("Generated date should be recent", result.getTime() >= before);
    assertTrue("Generated date should be recent", result.getTime() <= after);
  }

  @Test
  public void testDateGenerator_MultipleGenerations_ShouldReturnDifferentDates()
      throws InterruptedException {
    final DateGenerator generator = new DateGenerator();
    final Date date1 = generator.generateValue(TestEntity.class, mockDatabase);
    Thread.sleep(10); // Small delay to ensure different timestamps
    final Date date2 = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull(date1);
    assertNotNull(date2);
    assertTrue("Second date should be after or equal to first", date2.getTime() >= date1.getTime());
  }

  @Test
  public void testDateGenerator_WithNullDatabase_ShouldStillWork() {
    final DateGenerator generator = new DateGenerator();
    final Date result = generator.generateValue(TestEntity.class, null);

    assertNotNull("Should generate date even with null database", result);
  }

  @Test
  public void testDateGenerator_WithNullParentClass_ShouldStillWork() {
    final DateGenerator generator = new DateGenerator();
    final Date result = generator.generateValue(null, mockDatabase);

    assertNotNull("Should generate date even with null parent class", result);
  }

  @Test
  public void testDateGenerator_ImplementsGeneratorInterface() {
    final DateGenerator generator = new DateGenerator();
    assertTrue("Should implement Generator interface", generator instanceof Generator);
  }

  @Test
  public void testDateGenerator_ConsecutiveCalls_ShouldReturnNewInstances() {
    final DateGenerator generator = new DateGenerator();
    final Date date1 = generator.generateValue(TestEntity.class, mockDatabase);
    final Date date2 = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull(date1);
    assertNotNull(date2);
    // Even if timestamps are same, they should be different instances
    assertNotSame("Should return new Date instances", date1, date2);
  }

  @Test
  public void testDateGenerator_ThreadSafety() throws InterruptedException {
    final DateGenerator generator = new DateGenerator();
    final int threadCount = 10;
    final Date[] results = new Date[threadCount];
    final Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      threads[i] =
          new Thread(
              () -> {
                results[index] = generator.generateValue(TestEntity.class, mockDatabase);
              });
      threads[i].start();
    }

    for (final Thread thread : threads) {
      thread.join();
    }

    // All results should be non-null
    for (final Date result : results) {
      assertNotNull("All generated dates should be non-null", result);
    }
  }

  // ==================== IntegerGenerator Tests ====================

  @Test
  public void testIntegerGenerator_ImplementsGeneratorInterface() {
    final IntegerGenerator generator = new IntegerGenerator();
    assertTrue("Should implement Generator interface", generator instanceof Generator);
  }

  @Test
  public void testIntegerGenerator_GenerateValue_WithNoExistingValue_ShouldReturnOne() {
    final IntegerGenerator generator = new IntegerGenerator();

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    final Integer result = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull("Generated value should not be null", result);
    assertEquals("First generated value should be 1", Integer.valueOf(1), result);
    verify(mockCollection).insertOne(any(Document.class));
  }

  @Test
  public void testIntegerGenerator_GenerateValue_WithExistingValue_ShouldIncrement() {
    final IntegerGenerator generator = new IntegerGenerator();
    final Document existingDoc = new Document("generatedValue", 5).append("_id", new ObjectId());

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(existingDoc);

    final Integer result = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull("Generated value should not be null", result);
    assertEquals("Should increment existing value", Integer.valueOf(6), result);
    verify(mockCollection).replaceOne(any(Document.class), any(Document.class));
  }

  @Test
  public void testIntegerGenerator_CollectionName_ShouldUseParentClassName() {
    final IntegerGenerator generator = new IntegerGenerator();

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    generator.generateValue(TestEntity.class, mockDatabase);

    verify(mockDatabase).getCollection("values_TestEntity");
  }

  @Test
  public void testIntegerGenerator_WithZeroValue_ShouldReturnOne() {
    final IntegerGenerator generator = new IntegerGenerator();
    final Document existingDoc = new Document("generatedValue", 0).append("_id", new ObjectId());

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(existingDoc);

    final Integer result = generator.generateValue(TestEntity.class, mockDatabase);

    assertEquals("Should increment from zero to one", Integer.valueOf(1), result);
  }

  @Test
  public void testIntegerGenerator_WithoutObjectId_ShouldInsert() {
    final IntegerGenerator generator = new IntegerGenerator();
    final Document existingDoc = new Document("generatedValue", 3); // No _id

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(existingDoc);

    final Integer result = generator.generateValue(TestEntity.class, mockDatabase);

    assertEquals("Should increment value", Integer.valueOf(4), result);
    verify(mockCollection).insertOne(any(Document.class));
    verify(mockCollection, never()).replaceOne(any(Document.class), any(Document.class));
  }

  @Test
  public void testIntegerGenerator_ReturnType_ShouldBeInteger() {
    final IntegerGenerator generator = new IntegerGenerator();

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    final Object result = generator.generateValue(TestEntity.class, mockDatabase);

    assertTrue("Result should be Integer type", result instanceof Integer);
  }

  // ==================== Generator Interface Tests ====================

  @Test
  public void testGeneratorInterface_DateGeneratorImplementation() {
    final Generator generator = new DateGenerator();
    final Object result = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull(result);
    assertTrue("DateGenerator should return Date", result instanceof Date);
  }

  @Test
  public void testGeneratorInterface_IntegerGeneratorImplementation() {
    final Generator generator = new IntegerGenerator();

    when(mockDatabase.getCollection("values_TestEntity")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    final Object result = generator.generateValue(TestEntity.class, mockDatabase);

    assertNotNull(result);
    assertTrue("IntegerGenerator should return Integer", result instanceof Integer);
  }

  @Test
  public void testGeneratorInterface_Polymorphism() {
    final Generator dateGen = new DateGenerator();
    final Generator intGen = new IntegerGenerator();

    assertNotNull(dateGen);
    assertNotNull(intGen);
    assertNotSame("Different generator implementations", dateGen.getClass(), intGen.getClass());
  }
}
