package com.arquivolivre.mongocom.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for IntegerGenerator with mocked MongoDB. */
@RunWith(MockitoJUnitRunner.class)
public class IntegerGeneratorTest {

  @Mock private MongoDatabase mockDatabase;

  @Mock private MongoCollection<Document> mockCollection;

  private IntegerGenerator generator;

  @Before
  public void setUp() {
    generator = new IntegerGenerator();
  }

  @Test
  public void testGenerateValueFirstTime() {
    // Setup: No existing document
    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    // Execute
    Integer result = generator.generateValue(TestClass.class, mockDatabase);

    // Verify
    assertEquals(Integer.valueOf(1), result);
    verify(mockCollection).insertOne(any(Document.class));
  }

  @Test
  public void testGenerateValueWithExistingDocument() {
    // Setup: Existing document with value 5
    Document existingDoc = new Document("generatedValue", 5);
    existingDoc.put("_id", new ObjectId());

    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(existingDoc);

    // Execute
    Integer result = generator.generateValue(TestClass.class, mockDatabase);

    // Verify
    assertEquals(Integer.valueOf(6), result);
    verify(mockCollection).replaceOne(any(Document.class), any(Document.class));
  }

  @Test
  public void testGenerateValueIncrementsCorrectly() {
    // Setup: Existing document with value 10
    Document existingDoc = new Document("generatedValue", 10);
    existingDoc.put("_id", new ObjectId());

    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(existingDoc);

    // Execute
    Integer result = generator.generateValue(TestClass.class, mockDatabase);

    // Verify
    assertEquals(Integer.valueOf(11), result);
  }

  @Test
  public void testGenerateValueWithZeroValue() {
    // Setup: Existing document with value 0
    Document existingDoc = new Document("generatedValue", 0);
    existingDoc.put("_id", new ObjectId());

    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(existingDoc);

    // Execute
    Integer result = generator.generateValue(TestClass.class, mockDatabase);

    // Verify
    assertEquals(Integer.valueOf(1), result);
  }

  @Test
  public void testGenerateValueUsesCorrectCollectionName() {
    // Setup
    when(mockDatabase.getCollection("values_AnotherClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    // Execute
    generator.generateValue(AnotherClass.class, mockDatabase);

    // Verify collection name
    verify(mockDatabase).getCollection("values_AnotherClass");
  }

  @Test
  public void testGenerateValueWithoutObjectId() {
    // Setup: Document without _id (new document)
    Document newDoc = new Document("generatedValue", 0);

    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(newDoc);

    // Execute
    Integer result = generator.generateValue(TestClass.class, mockDatabase);

    // Verify: Should insert instead of replace
    assertEquals(Integer.valueOf(1), result);
    verify(mockCollection).insertOne(any(Document.class));
    verify(mockCollection, never()).replaceOne(any(Document.class), any(Document.class));
  }

  @Test
  public void testGenerateValueMultipleCalls() {
    // Setup: Simulate multiple calls
    Document doc1 = new Document("generatedValue", 1);
    doc1.put("_id", new ObjectId());

    Document doc2 = new Document("generatedValue", 2);
    doc2.put("_id", new ObjectId());

    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(doc1, doc2);

    // Execute
    Integer result1 = generator.generateValue(TestClass.class, mockDatabase);
    Integer result2 = generator.generateValue(TestClass.class, mockDatabase);

    // Verify
    assertEquals(Integer.valueOf(2), result1);
    assertEquals(Integer.valueOf(3), result2);
  }

  @Test
  public void testImplementsGeneratorInterface() {
    assertTrue(generator instanceof Generator);
  }

  @Test
  public void testGenerateValueWithDifferentParentClasses() {
    // Setup for TestClass
    when(mockDatabase.getCollection("values_TestClass")).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(mockCollection.find().first()).thenReturn(null);

    // Execute
    Integer result1 = generator.generateValue(TestClass.class, mockDatabase);

    // Setup for AnotherClass
    MongoCollection<Document> anotherCollection = mock(MongoCollection.class);
    when(mockDatabase.getCollection("values_AnotherClass")).thenReturn(anotherCollection);
    when(anotherCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
    when(anotherCollection.find().first()).thenReturn(null);

    // Execute
    Integer result2 = generator.generateValue(AnotherClass.class, mockDatabase);

    // Verify both return 1 (independent sequences)
    assertEquals(Integer.valueOf(1), result1);
    assertEquals(Integer.valueOf(1), result2);
  }

  // Test helper classes
  private static class TestClass {}

  private static class AnotherClass {}
}