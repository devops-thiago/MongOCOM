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

package com.arquivolivre.mongocom.indexes;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import com.arquivolivre.mongocom.exception.MappingException;
import com.mongodb.MongoException;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import java.util.Set;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for IndexManager.
 *
 * <p>Tests index management functionality including creation, deletion, and listing with mocked
 * MongoDB operations.
 */
@RunWith(MockitoJUnitRunner.class)
public class IndexManagerTest {

  @Mock private MongoConnectionManager mockConnectionManager;
  @Mock private MongoDatabase mockDatabase;
  @Mock private MongoCollection<org.bson.Document> mockCollection;
  @Mock private ListIndexesIterable<org.bson.Document> mockListIndexes;
  @Mock private MongoCursor<org.bson.Document> mockCursor;

  private IndexManager indexManager;

  @Document(collection = "test_users")
  private static class TestUser {
    @Id private String id;

    @Index(type = "text")
    private String name;

    @Index(unique = true)
    private String email;

    @Index(order = -1)
    private int age;
  }

  @Document(collection = "test_products")
  private static class TestProduct {
    @Id private String id;

    @Index(sparse = true)
    private String sku;

    @Index(type = "hashed")
    private String category;
  }

  @Document(collection = "test_locations")
  private static class TestLocation {
    @Id private String id;

    @Index(type = "2dsphere")
    private String coordinates;
  }

  @Document(collection = "test_geo")
  private static class TestGeo {
    @Id private String id;

    @Index(type = "2d")
    private String location;
  }

  @Document(collection = "test_simple")
  private static class TestSimple {
    @Id private String id;
    private String name; // No indexes
  }

  @Document(collection = "test_background")
  private static class TestBackground {
    @Id private String id;

    @Index(background = true)
    private String field;
  }

  @Before
  public void setUp() {
    indexManager = new IndexManager(mockConnectionManager);
    when(mockConnectionManager.getDefaultDatabase()).thenReturn(mockDatabase);
    when(mockDatabase.getCollection(anyString())).thenReturn(mockCollection);
  }

  // ==================== Constructor Tests ====================

  @Test(expected = NullPointerException.class)
  public void testConstructor_WithNullConnectionManager_ShouldThrowException() {
    new IndexManager(null);
  }

  @Test
  public void testConstructor_WithValidConnectionManager_ShouldCreateManager() {
    final IndexManager manager = new IndexManager(mockConnectionManager);
    assertNotNull(manager);
    assertEquals(0, manager.getEnsuredCount());
  }

  // ==================== EnsureIndexes Tests ====================

  @Test(expected = NullPointerException.class)
  public void testEnsureIndexes_WithNullClass_ShouldThrowException() {
    indexManager.ensureIndexes(null);
  }

  @Test
  public void testEnsureIndexes_WithEntityWithIndexes_ShouldCreateIndexes() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    indexManager.ensureIndexes(TestUser.class);

    // Should create 3 indexes (name, email, age)
    verify(mockCollection, times(3)).createIndex(any(Bson.class), any(IndexOptions.class));
    assertEquals(1, indexManager.getEnsuredCount());
  }

  @Test
  public void testEnsureIndexes_CalledTwice_ShouldOnlyCreateIndexesOnce() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    indexManager.ensureIndexes(TestUser.class);
    indexManager.ensureIndexes(TestUser.class);

    // Should only create indexes once
    verify(mockCollection, times(3)).createIndex(any(Bson.class), any(IndexOptions.class));
    assertEquals(1, indexManager.getEnsuredCount());
  }

  @Test
  public void testEnsureIndexes_WithEntityWithoutIndexes_ShouldNotCreateIndexes() {
    indexManager.ensureIndexes(TestSimple.class);

    verify(mockCollection, never()).createIndex(any(Bson.class), any(IndexOptions.class));
    assertEquals(1, indexManager.getEnsuredCount());
  }

  @Test
  public void testEnsureIndexes_WithMultipleEntities_ShouldTrackSeparately() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    indexManager.ensureIndexes(TestUser.class);
    indexManager.ensureIndexes(TestProduct.class);

    assertEquals(2, indexManager.getEnsuredCount());
  }

  @Test
  public void testEnsureIndexes_WithTextIndex_ShouldCreateTextIndex() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("name_text");

    indexManager.ensureIndexes(TestUser.class);

    verify(mockCollection, atLeastOnce()).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_WithHashedIndex_ShouldCreateHashedIndex() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("category_hashed");

    indexManager.ensureIndexes(TestProduct.class);

    verify(mockCollection, times(2)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_With2dsphereIndex_ShouldCreate2dsphereIndex() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("coordinates_2dsphere");

    indexManager.ensureIndexes(TestLocation.class);

    verify(mockCollection, times(1)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_With2dIndex_ShouldCreate2dIndex() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("location_2d");

    indexManager.ensureIndexes(TestGeo.class);

    verify(mockCollection, times(1)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_WithUniqueIndex_ShouldSetUniqueOption() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("email_unique");

    indexManager.ensureIndexes(TestUser.class);

    verify(mockCollection, times(3)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_WithSparseIndex_ShouldSetSparseOption() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("sku_sparse");

    indexManager.ensureIndexes(TestProduct.class);

    verify(mockCollection, times(2)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_WithBackgroundIndex_ShouldSetBackgroundOption() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("field_background");

    indexManager.ensureIndexes(TestBackground.class);

    verify(mockCollection, times(1)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  public void testEnsureIndexes_WithDescendingOrder_ShouldCreateDescendingIndex() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("age_desc");

    indexManager.ensureIndexes(TestUser.class);

    verify(mockCollection, times(3)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test(expected = MappingException.class)
  public void testEnsureIndexes_WhenCreateIndexFails_ShouldThrowMappingException() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenThrow(new MongoException("Index creation failed"));

    indexManager.ensureIndexes(TestUser.class);
  }

  @Test
  public void testEnsureIndexes_AfterClear_ShouldCreateIndexesAgain() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    indexManager.ensureIndexes(TestUser.class);
    indexManager.clearEnsuredIndexes();
    indexManager.ensureIndexes(TestUser.class);

    // Should create indexes twice (3 indexes each time)
    verify(mockCollection, times(6)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  // ==================== DropIndexes Tests ====================

  @Test(expected = NullPointerException.class)
  public void testDropIndexes_WithNullClass_ShouldThrowException() {
    indexManager.dropIndexes(null);
  }

  @Test
  public void testDropIndexes_WithValidClass_ShouldDropIndexes() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    indexManager.ensureIndexes(TestUser.class);
    assertEquals(1, indexManager.getEnsuredCount());

    indexManager.dropIndexes(TestUser.class);

    verify(mockCollection).dropIndexes();
    assertEquals(0, indexManager.getEnsuredCount());
  }

  @Test
  public void testDropIndexes_WithoutEnsuring_ShouldStillDropIndexes() {
    indexManager.dropIndexes(TestUser.class);

    verify(mockCollection).dropIndexes();
    assertEquals(0, indexManager.getEnsuredCount());
  }

  @Test(expected = MappingException.class)
  public void testDropIndexes_WhenDropFails_ShouldThrowMappingException() {
    doThrow(new MongoException("Drop failed")).when(mockCollection).dropIndexes();

    indexManager.dropIndexes(TestUser.class);
  }

  @Test
  public void testDropIndexes_CalledTwice_ShouldDropTwice() {
    indexManager.dropIndexes(TestUser.class);
    indexManager.dropIndexes(TestUser.class);

    verify(mockCollection, times(2)).dropIndexes();
  }

  @Test
  public void testDropIndexes_AfterEnsure_ShouldRemoveFromTracking() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    indexManager.ensureIndexes(TestUser.class); // 3 indexes
    indexManager.ensureIndexes(TestProduct.class); // 2 indexes
    assertEquals(2, indexManager.getEnsuredCount());

    indexManager.dropIndexes(TestUser.class);
    assertEquals(1, indexManager.getEnsuredCount());

    // Should be able to ensure again (3 more indexes)
    indexManager.ensureIndexes(TestUser.class);
    // Total: 3 (TestUser first) + 2 (TestProduct) + 3 (TestUser again) = 8
    verify(mockCollection, times(8)).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  // ==================== ListIndexes Tests ====================

  @Test(expected = NullPointerException.class)
  public void testListIndexes_WithNullClass_ShouldThrowException() {
    indexManager.listIndexes(null);
  }

  @Test
  public void testListIndexes_WithNoIndexes_ShouldReturnEmptySet() {
    when(mockCollection.listIndexes()).thenReturn(mockListIndexes);
    when(mockListIndexes.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(false);

    final Set<String> indexes = indexManager.listIndexes(TestUser.class);

    assertNotNull(indexes);
    assertTrue(indexes.isEmpty());
  }

  @Test
  public void testListIndexes_WithIndexes_ShouldReturnIndexNames() {
    final org.bson.Document doc1 = new org.bson.Document("name", "_id_");
    final org.bson.Document doc2 = new org.bson.Document("name", "email_1");
    final org.bson.Document doc3 = new org.bson.Document("name", "name_text");

    when(mockCollection.listIndexes()).thenReturn(mockListIndexes);
    when(mockListIndexes.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true, true, true, false);
    when(mockCursor.next()).thenReturn(doc1, doc2, doc3);

    final Set<String> indexes = indexManager.listIndexes(TestUser.class);

    assertNotNull(indexes);
    assertEquals(3, indexes.size());
    assertTrue(indexes.contains("_id_"));
    assertTrue(indexes.contains("email_1"));
    assertTrue(indexes.contains("name_text"));
  }

  @Test
  public void testListIndexes_WithNullIndexName_ShouldSkipNull() {
    final org.bson.Document doc1 = new org.bson.Document("name", "email_1");
    final org.bson.Document doc2 = new org.bson.Document("key", "value"); // No "name" field
    final org.bson.Document doc3 = new org.bson.Document("name", "age_-1");

    when(mockCollection.listIndexes()).thenReturn(mockListIndexes);
    when(mockListIndexes.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true, true, true, false);
    when(mockCursor.next()).thenReturn(doc1, doc2, doc3);

    final Set<String> indexes = indexManager.listIndexes(TestUser.class);

    assertNotNull(indexes);
    assertEquals(2, indexes.size());
    assertTrue(indexes.contains("email_1"));
    assertTrue(indexes.contains("age_-1"));
  }

  @Test(expected = MappingException.class)
  public void testListIndexes_WhenListFails_ShouldThrowMappingException() {
    when(mockCollection.listIndexes()).thenThrow(new MongoException("List failed"));

    indexManager.listIndexes(TestUser.class);
  }

  @Test
  public void testListIndexes_CalledMultipleTimes_ShouldQueryEachTime() {
    final org.bson.Document doc1 = new org.bson.Document("name", "index1");

    when(mockCollection.listIndexes()).thenReturn(mockListIndexes);
    when(mockListIndexes.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true, false);
    when(mockCursor.next()).thenReturn(doc1);

    indexManager.listIndexes(TestUser.class);
    indexManager.listIndexes(TestUser.class);

    verify(mockCollection, times(2)).listIndexes();
  }

  // ==================== ClearEnsuredIndexes Tests ====================

  @Test
  public void testClearEnsuredIndexes_ShouldResetCount() {
    indexManager.clearEnsuredIndexes();
    assertEquals(0, indexManager.getEnsuredCount());
  }

  @Test
  public void testClearEnsuredIndexes_AfterMultipleCalls_ShouldWork() {
    indexManager.clearEnsuredIndexes();
    indexManager.clearEnsuredIndexes();
    indexManager.clearEnsuredIndexes();
    assertEquals(0, indexManager.getEnsuredCount());
  }

  @Test
  public void testClearEnsuredIndexes_Idempotent() {
    indexManager.clearEnsuredIndexes();
    final int count1 = indexManager.getEnsuredCount();

    indexManager.clearEnsuredIndexes();
    final int count2 = indexManager.getEnsuredCount();

    assertEquals("Clear should be idempotent", count1, count2);
    assertEquals("Count should be zero", 0, count2);
  }

  // ==================== GetEnsuredCount Tests ====================

  @Test
  public void testGetEnsuredCount_Initially_ShouldBeZero() {
    assertEquals(0, indexManager.getEnsuredCount());
  }

  @Test
  public void testGetEnsuredCount_ConsistentAcrossMultipleCalls() {
    final int count1 = indexManager.getEnsuredCount();
    final int count2 = indexManager.getEnsuredCount();
    final int count3 = indexManager.getEnsuredCount();

    assertEquals("Count should be consistent", count1, count2);
    assertEquals("Count should be consistent", count2, count3);
  }

  @Test
  public void testGetEnsuredCount_AfterEnsure_ShouldIncrement() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    assertEquals(0, indexManager.getEnsuredCount());

    indexManager.ensureIndexes(TestUser.class);
    assertEquals(1, indexManager.getEnsuredCount());

    indexManager.ensureIndexes(TestProduct.class);
    assertEquals(2, indexManager.getEnsuredCount());
  }

  // ==================== ToString Tests ====================

  @Test
  public void testToString_ShouldContainEnsuredCount() {
    final String str = indexManager.toString();
    assertNotNull(str);
    assertTrue("Should contain class name", str.contains("IndexManager"));
    assertTrue("Should contain ensured count", str.contains("0"));
  }

  @Test
  public void testToString_Format() {
    final String str = indexManager.toString();
    assertTrue("Should start with class name", str.startsWith("IndexManager{"));
    assertTrue("Should end with brace", str.endsWith("}"));
    assertTrue("Should contain ensuredIndexes", str.contains("ensuredIndexes="));
  }

  @Test
  public void testToString_AfterClear_ShouldShowZero() {
    indexManager.clearEnsuredIndexes();
    final String str = indexManager.toString();
    assertTrue("Should show zero count", str.contains("0"));
  }

  @Test
  public void testToString_DifferentStates() {
    final String str1 = indexManager.toString();
    indexManager.clearEnsuredIndexes();
    final String str2 = indexManager.toString();

    assertNotNull(str1);
    assertNotNull(str2);
    assertTrue(str1.contains("0"));
    assertTrue(str2.contains("0"));
  }

  // ==================== Thread Safety Tests ====================

  @Test
  public void testGetEnsuredCount_ShouldBeThreadSafe() throws InterruptedException {
    final int threadCount = 10;
    final Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      threads[i] =
          new Thread(
              () -> {
                final int count = indexManager.getEnsuredCount();
                assertTrue("Count should be non-negative", count >= 0);
              });
      threads[i].start();
    }

    for (final Thread thread : threads) {
      thread.join();
    }
  }

  @Test
  public void testClearEnsuredIndexes_ShouldBeThreadSafe() throws InterruptedException {
    final int threadCount = 10;
    final Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      threads[i] = new Thread(() -> indexManager.clearEnsuredIndexes());
      threads[i].start();
    }

    for (final Thread thread : threads) {
      thread.join();
    }

    assertEquals(0, indexManager.getEnsuredCount());
  }

  @Test
  public void testConcurrentClear_ShouldNotCauseErrors() throws InterruptedException {
    final int iterations = 100;
    final Thread clearThread =
        new Thread(
            () -> {
              for (int i = 0; i < iterations; i++) {
                indexManager.clearEnsuredIndexes();
              }
            });

    final Thread countThread =
        new Thread(
            () -> {
              for (int i = 0; i < iterations; i++) {
                indexManager.getEnsuredCount();
              }
            });

    clearThread.start();
    countThread.start();

    clearThread.join();
    countThread.join();

    assertEquals(0, indexManager.getEnsuredCount());
  }

  // ==================== Integration Tests ====================

  @Test
  public void testMultipleManagers_ShouldBeIndependent() {
    final IndexManager manager1 = new IndexManager(mockConnectionManager);
    final IndexManager manager2 = new IndexManager(mockConnectionManager);

    assertEquals(0, manager1.getEnsuredCount());
    assertEquals(0, manager2.getEnsuredCount());

    manager1.clearEnsuredIndexes();
    assertEquals(0, manager1.getEnsuredCount());
    assertEquals(0, manager2.getEnsuredCount());
  }

  @Test
  public void testConstructor_CreatesNewMetadataExtractor() {
    final IndexManager manager1 = new IndexManager(mockConnectionManager);
    final IndexManager manager2 = new IndexManager(mockConnectionManager);

    assertNotNull(manager1);
    assertNotNull(manager2);
  }

  @Test
  public void testManagerLifecycle() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    // Create
    final IndexManager manager = new IndexManager(mockConnectionManager);
    assertEquals(0, manager.getEnsuredCount());

    // Ensure indexes
    manager.ensureIndexes(TestUser.class);
    assertEquals(1, manager.getEnsuredCount());

    // Drop indexes
    manager.dropIndexes(TestUser.class);
    assertEquals(0, manager.getEnsuredCount());

    // Clear
    manager.clearEnsuredIndexes();
    assertEquals(0, manager.getEnsuredCount());
  }

  @Test
  public void testManagerState_AfterConstruction() {
    final IndexManager manager = new IndexManager(mockConnectionManager);
    assertEquals("Initial count should be zero", 0, manager.getEnsuredCount());
    assertNotNull("ToString should not be null", manager.toString());
  }

  @Test
  public void testCompleteWorkflow() {
    when(mockCollection.createIndex(any(Bson.class), any(IndexOptions.class)))
        .thenReturn("index_name");

    final org.bson.Document doc1 = new org.bson.Document("name", "_id_");
    final org.bson.Document doc2 = new org.bson.Document("name", "email_1");

    when(mockCollection.listIndexes()).thenReturn(mockListIndexes);
    when(mockListIndexes.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true, true, false);
    when(mockCursor.next()).thenReturn(doc1, doc2);

    // Ensure indexes
    indexManager.ensureIndexes(TestUser.class);
    assertEquals(1, indexManager.getEnsuredCount());

    // List indexes
    final Set<String> indexes = indexManager.listIndexes(TestUser.class);
    assertEquals(2, indexes.size());

    // Drop indexes
    indexManager.dropIndexes(TestUser.class);
    assertEquals(0, indexManager.getEnsuredCount());

    // Ensure again
    indexManager.ensureIndexes(TestUser.class);
    assertEquals(1, indexManager.getEnsuredCount());
  }
}
