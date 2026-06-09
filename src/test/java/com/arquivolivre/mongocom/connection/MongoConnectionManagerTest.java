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

package com.arquivolivre.mongocom.connection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MongoConnectionManager.
 *
 * <p>Tests the singleton pattern, thread safety, and database caching functionality using mocks.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class MongoConnectionManagerTest {

  private MongoClient mockClient;
  private MongoDatabase mockDatabase;

  @Before
  public void setUp() {
    mockClient = mock(MongoClient.class);
    mockDatabase = mock(MongoDatabase.class);

    // Reset singleton instance before each test
    MongoConnectionManager.resetInstance();
  }

  @After
  public void tearDown() {
    // Clean up singleton instance after each test
    MongoConnectionManager.resetInstance();
  }

  // ==================== Constructor and Singleton Tests ====================

  @Test(expected = NullPointerException.class)
  public void testGetInstance_WithNullClient_ShouldThrowException() {
    MongoConnectionManager.getInstance(null, "test_db");
  }

  @Test
  public void testGetInstance_WithValidClient_ShouldCreateInstance() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    final MongoConnectionManager manager =
        MongoConnectionManager.getInstance(mockClient, "test_db");

    assertNotNull("Manager should not be null", manager);
    verify(mockClient).getDatabase("test_db");
  }

  @Test
  public void testGetInstance_WithNullDatabaseName_ShouldCreateInstance() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    assertNotNull("Manager should not be null", manager);
    verifyNoInteractions(mockClient);
  }

  @Test
  public void testGetInstance_WithEmptyDatabaseName_ShouldCreateInstance() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, "");

    assertNotNull("Manager should not be null", manager);
    verifyNoInteractions(mockClient);
  }

  @Test
  public void testGetInstance_CalledTwice_ShouldReturnSameInstance() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    final MongoConnectionManager manager1 =
        MongoConnectionManager.getInstance(mockClient, "test_db");
    final MongoConnectionManager manager2 =
        MongoConnectionManager.getInstance(mockClient, "test_db");

    assertSame("Should return same instance", manager1, manager2);
    // Database should only be accessed once (during first initialization)
    verify(mockClient, times(1)).getDatabase("test_db");
  }

  @Test(expected = IllegalStateException.class)
  public void testGetInstance_WithoutInitialization_ShouldThrowException() {
    MongoConnectionManager.getInstance();
  }

  @Test
  public void testGetInstance_AfterInitialization_ShouldReturnInstance() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    MongoConnectionManager.getInstance(mockClient, "test_db");
    final MongoConnectionManager manager = MongoConnectionManager.getInstance();

    assertNotNull("Manager should not be null", manager);
  }

  // ==================== Database Access Tests ====================

  @Test
  public void testGetDefaultDatabase_WithDefaultSet_ShouldReturnDatabase() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    final MongoConnectionManager manager =
        MongoConnectionManager.getInstance(mockClient, "test_db");
    final MongoDatabase db = manager.getDefaultDatabase();

    assertNotNull("Database should not be null", db);
    assertSame("Should return same database", mockDatabase, db);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetDefaultDatabase_WithoutDefaultSet_ShouldThrowException() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    manager.getDefaultDatabase();
  }

  @Test
  public void testGetDatabase_WithValidName_ShouldReturnDatabase() {
    final MongoDatabase customDb = mock(MongoDatabase.class);
    when(mockClient.getDatabase("custom_db")).thenReturn(customDb);

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);
    final MongoDatabase db = manager.getDatabase("custom_db");

    assertNotNull("Database should not be null", db);
    assertSame("Should return custom database", customDb, db);
    verify(mockClient).getDatabase("custom_db");
  }

  @Test(expected = NullPointerException.class)
  public void testGetDatabase_WithNullName_ShouldThrowException() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    manager.getDatabase(null);
  }

  @Test
  public void testGetDatabase_CalledTwice_ShouldCacheDatabase() {
    final MongoDatabase customDb = mock(MongoDatabase.class);
    when(mockClient.getDatabase("custom_db")).thenReturn(customDb);

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    final MongoDatabase db1 = manager.getDatabase("custom_db");
    final MongoDatabase db2 = manager.getDatabase("custom_db");

    assertSame("Should return cached database", db1, db2);
    // Should only call getDatabase once (cached after first call)
    verify(mockClient, times(1)).getDatabase("custom_db");
  }

  @Test
  public void testGetDatabase_MultipleDatabases_ShouldCacheEach() {
    final MongoDatabase db1 = mock(MongoDatabase.class);
    final MongoDatabase db2 = mock(MongoDatabase.class);
    when(mockClient.getDatabase("db1")).thenReturn(db1);
    when(mockClient.getDatabase("db2")).thenReturn(db2);

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    final MongoDatabase result1 = manager.getDatabase("db1");
    final MongoDatabase result2 = manager.getDatabase("db2");
    final MongoDatabase result1Again = manager.getDatabase("db1");

    assertSame("Should return db1", db1, result1);
    assertSame("Should return db2", db2, result2);
    assertSame("Should return cached db1", db1, result1Again);

    verify(mockClient, times(1)).getDatabase("db1");
    verify(mockClient, times(1)).getDatabase("db2");
  }

  // ==================== Cache Management Tests ====================

  @Test
  public void testGetCacheSize_Initially_ShouldBeZero() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    assertEquals("Cache should be empty", 0, manager.getCacheSize());
  }

  @Test
  public void testGetCacheSize_AfterDatabaseAccess_ShouldIncrement() {
    when(mockClient.getDatabase("db1")).thenReturn(mockDatabase);

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    manager.getDatabase("db1");

    assertEquals("Cache should have 1 entry", 1, manager.getCacheSize());
  }

  @Test
  public void testIsCached_ForCachedDatabase_ShouldReturnTrue() {
    when(mockClient.getDatabase("db1")).thenReturn(mockDatabase);

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    manager.getDatabase("db1");

    assertTrue("Database should be cached", manager.isCached("db1"));
  }

  @Test
  public void testIsCached_ForUncachedDatabase_ShouldReturnFalse() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    assertFalse("Database should not be cached", manager.isCached("uncached_db"));
  }

  @Test
  public void testClearCache_ShouldRemoveAllEntries() {
    when(mockClient.getDatabase("db1")).thenReturn(mockDatabase);
    when(mockClient.getDatabase("db2")).thenReturn(mock(MongoDatabase.class));

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    manager.getDatabase("db1");
    manager.getDatabase("db2");
    assertEquals("Cache should have 2 entries", 2, manager.getCacheSize());

    manager.clearCache();

    assertEquals("Cache should be empty", 0, manager.getCacheSize());
  }

  @Test
  public void testGetClient_ShouldReturnClient() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    assertSame("Should return same client", mockClient, manager.getClient());
  }

  @Test
  public void testGetDefaultDatabaseName_WithDefault_ShouldReturnName() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    final MongoConnectionManager manager =
        MongoConnectionManager.getInstance(mockClient, "test_db");

    assertEquals(
        "Should return default database name", "test_db", manager.getDefaultDatabaseName());
  }

  @Test
  public void testGetDefaultDatabaseName_WithoutDefault_ShouldReturnNull() {
    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    assertNull("Should return null", manager.getDefaultDatabaseName());
  }

  // ==================== Thread Safety Tests ====================

  @Test
  public void testGetInstance_ConcurrentAccess_ShouldReturnSameInstance()
      throws InterruptedException {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    final int threadCount = 10;
    final Thread[] threads = new Thread[threadCount];
    final MongoConnectionManager[] managers = new MongoConnectionManager[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      threads[i] =
          new Thread(
              () -> {
                managers[index] = MongoConnectionManager.getInstance(mockClient, "test_db");
              });
      threads[i].start();
    }

    for (final Thread thread : threads) {
      thread.join();
    }

    // All threads should get the same instance
    for (int i = 1; i < threadCount; i++) {
      assertSame("All threads should get same instance", managers[0], managers[i]);
    }

    // Database should only be accessed once
    verify(mockClient, times(1)).getDatabase("test_db");
  }

  @Test
  public void testGetDatabase_ConcurrentAccess_ShouldCacheCorrectly() throws InterruptedException {
    final MongoDatabase customDb = mock(MongoDatabase.class);
    when(mockClient.getDatabase("custom_db")).thenReturn(customDb);

    final MongoConnectionManager manager = MongoConnectionManager.getInstance(mockClient, null);

    final int threadCount = 10;
    final Thread[] threads = new Thread[threadCount];
    final MongoDatabase[] databases = new MongoDatabase[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      threads[i] =
          new Thread(
              () -> {
                databases[index] = manager.getDatabase("custom_db");
              });
      threads[i].start();
    }

    for (final Thread thread : threads) {
      thread.join();
    }

    // All threads should get the same database instance
    for (int i = 1; i < threadCount; i++) {
      assertSame("All threads should get same database", databases[0], databases[i]);
    }

    // Database should only be accessed once (cached)
    verify(mockClient, times(1)).getDatabase("custom_db");
  }

  // ==================== Close Tests ====================

  @Test
  public void testClose_ShouldCloseClient() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);

    final MongoConnectionManager manager =
        MongoConnectionManager.getInstance(mockClient, "test_db");

    manager.close();

    verify(mockClient).close();
  }

  @Test
  public void testClose_ShouldClearCache() {
    when(mockClient.getDatabase("test_db")).thenReturn(mockDatabase);
    when(mockClient.getDatabase("db1")).thenReturn(mock(MongoDatabase.class));

    final MongoConnectionManager manager =
        MongoConnectionManager.getInstance(mockClient, "test_db");

    manager.getDatabase("db1");
    assertEquals("Cache should have 2 entries", 2, manager.getCacheSize());

    manager.close();

    verify(mockClient).close();
    assertEquals("Cache should be cleared", 0, manager.getCacheSize());
  }
}
