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

package com.arquivolivre.mongocom.repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for RepositoryFactory.
 *
 * <p>Tests repository creation, caching, thread safety, and factory methods.
 */
public class RepositoryFactoryTest {

  private MongoConnectionManager mockConnectionManager;
  private RepositoryFactory factory;

  @Document(collection = "test_users")
  private static class TestUser {
    @Id private String id;
    private String name;
  }

  @Document(collection = "test_products")
  private static class TestProduct {
    @Id private String id;
    private String name;
    private double price;
  }

  @Document(collection = "test_orders")
  private static class TestOrder {
    @Id private String id;
    private String userId;
    private double total;
  }

  @Before
  public void setUp() {
    mockConnectionManager = mock(MongoConnectionManager.class);
    factory = new RepositoryFactory(mockConnectionManager);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_WithNullConnectionManager_ShouldThrowException() {
    new RepositoryFactory(null);
  }

  @Test
  public void testConstructor_WithValidConnectionManager_ShouldCreateFactory() {
    final RepositoryFactory newFactory = new RepositoryFactory(mockConnectionManager);
    assertNotNull(newFactory);
    assertEquals(0, newFactory.getCacheSize());
  }

  @Test(expected = NullPointerException.class)
  public void testGetRepository_WithNullClass_ShouldThrowException() {
    factory.getRepository(null);
  }

  @Test
  public void testGetRepository_FirstCall_ShouldCreateRepository() {
    final EntityRepository<TestUser, String> repository = factory.getRepository(TestUser.class);

    assertNotNull(repository);
    assertEquals(TestUser.class, repository.getEntityClass());
    assertEquals("test_users", repository.getCollectionName());
    assertEquals(1, factory.getCacheSize());
  }

  @Test
  public void testGetRepository_SecondCall_ShouldReturnCachedRepository() {
    final EntityRepository<TestUser, String> repository1 = factory.getRepository(TestUser.class);
    final EntityRepository<TestUser, String> repository2 = factory.getRepository(TestUser.class);

    assertSame("Should return same cached instance", repository1, repository2);
    assertEquals(1, factory.getCacheSize());
  }

  @Test
  public void testGetRepository_DifferentClasses_ShouldCreateDifferentRepositories() {
    final EntityRepository<TestUser, String> userRepo = factory.getRepository(TestUser.class);
    final EntityRepository<TestProduct, String> productRepo =
        factory.getRepository(TestProduct.class);

    assertNotNull(userRepo);
    assertNotNull(productRepo);
    assertNotSame("Should be different instances", userRepo, productRepo);
    assertEquals(2, factory.getCacheSize());
  }

  @Test
  public void testGetRepository_MultipleClasses_ShouldCacheAll() {
    final EntityRepository<TestUser, String> userRepo = factory.getRepository(TestUser.class);
    final EntityRepository<TestProduct, String> productRepo =
        factory.getRepository(TestProduct.class);
    final EntityRepository<TestOrder, String> orderRepo = factory.getRepository(TestOrder.class);

    assertNotNull(userRepo);
    assertNotNull(productRepo);
    assertNotNull(orderRepo);
    assertEquals(3, factory.getCacheSize());
  }

  @Test
  public void testIsCached_WithCachedClass_ShouldReturnTrue() {
    factory.getRepository(TestUser.class);
    assertTrue(factory.isCached(TestUser.class));
  }

  @Test
  public void testIsCached_WithUncachedClass_ShouldReturnFalse() {
    assertFalse(factory.isCached(TestUser.class));
  }

  @Test
  public void testIsCached_AfterClearCache_ShouldReturnFalse() {
    factory.getRepository(TestUser.class);
    assertTrue(factory.isCached(TestUser.class));

    factory.clearCache();
    assertFalse(factory.isCached(TestUser.class));
  }

  @Test
  public void testClearCache_ShouldRemoveAllRepositories() {
    factory.getRepository(TestUser.class);
    factory.getRepository(TestProduct.class);
    factory.getRepository(TestOrder.class);
    assertEquals(3, factory.getCacheSize());

    factory.clearCache();
    assertEquals(0, factory.getCacheSize());
  }

  @Test
  public void testClearCache_AfterClear_ShouldCreateNewRepositories() {
    final EntityRepository<TestUser, String> repo1 = factory.getRepository(TestUser.class);
    factory.clearCache();
    final EntityRepository<TestUser, String> repo2 = factory.getRepository(TestUser.class);

    assertNotSame("Should create new instance after cache clear", repo1, repo2);
  }

  @Test
  public void testGetCacheSize_InitiallyZero() {
    assertEquals(0, factory.getCacheSize());
  }

  @Test
  public void testGetCacheSize_AfterAddingRepositories() {
    assertEquals(0, factory.getCacheSize());
    factory.getRepository(TestUser.class);
    assertEquals(1, factory.getCacheSize());
    factory.getRepository(TestProduct.class);
    assertEquals(2, factory.getCacheSize());
  }

  @Test
  public void testPreloadRepositories_WithNoClasses_ShouldNotFail() {
    factory.preloadRepositories();
    assertEquals(0, factory.getCacheSize());
  }

  @Test
  public void testPreloadRepositories_WithSingleClass_ShouldCacheRepository() {
    factory.preloadRepositories(TestUser.class);
    assertEquals(1, factory.getCacheSize());
    assertTrue(factory.isCached(TestUser.class));
  }

  @Test
  public void testPreloadRepositories_WithMultipleClasses_ShouldCacheAll() {
    factory.preloadRepositories(TestUser.class, TestProduct.class, TestOrder.class);
    assertEquals(3, factory.getCacheSize());
    assertTrue(factory.isCached(TestUser.class));
    assertTrue(factory.isCached(TestProduct.class));
    assertTrue(factory.isCached(TestOrder.class));
  }

  @Test
  public void testPreloadRepositories_CalledTwice_ShouldNotDuplicate() {
    factory.preloadRepositories(TestUser.class);
    factory.preloadRepositories(TestUser.class);
    assertEquals(1, factory.getCacheSize());
  }

  @Test
  public void testToString_ShouldContainCacheSize() {
    factory.getRepository(TestUser.class);
    factory.getRepository(TestProduct.class);

    final String str = factory.toString();
    assertNotNull(str);
    assertTrue("Should contain class name", str.contains("RepositoryFactory"));
    assertTrue("Should contain cache size", str.contains("2"));
  }

  @Test
  public void testThreadSafety_ConcurrentAccess_ShouldCreateOnlyOneRepository()
      throws InterruptedException {
    final int threadCount = 10;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);
    final AtomicInteger creationCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              factory.getRepository(TestUser.class);
              creationCount.incrementAndGet();
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(5, TimeUnit.SECONDS);
    executor.shutdown();

    assertEquals("All threads should complete", threadCount, creationCount.get());
    assertEquals("Should create only one repository", 1, factory.getCacheSize());
  }

  @Test
  public void testThreadSafety_ConcurrentDifferentClasses_ShouldCreateMultipleRepositories()
      throws InterruptedException {
    final int threadCount = 9; // 3 threads per class
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    // 3 threads for each class
    for (int i = 0; i < 3; i++) {
      executor.submit(
          () -> {
            try {
              factory.getRepository(TestUser.class);
            } finally {
              latch.countDown();
            }
          });
      executor.submit(
          () -> {
            try {
              factory.getRepository(TestProduct.class);
            } finally {
              latch.countDown();
            }
          });
      executor.submit(
          () -> {
            try {
              factory.getRepository(TestOrder.class);
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(5, TimeUnit.SECONDS);
    executor.shutdown();

    assertEquals("Should create exactly 3 repositories", 3, factory.getCacheSize());
    assertTrue(factory.isCached(TestUser.class));
    assertTrue(factory.isCached(TestProduct.class));
    assertTrue(factory.isCached(TestOrder.class));
  }

  @Test
  public void testRepositoryType_ShouldBeMongoEntityRepository() {
    final EntityRepository<TestUser, String> repository = factory.getRepository(TestUser.class);
    assertTrue(
        "Should be MongoEntityRepository instance", repository instanceof MongoEntityRepository);
  }

  @Test
  public void testGetRepository_WithGenericTypes_ShouldWork() {
    final EntityRepository<TestUser, String> repository = factory.getRepository(TestUser.class);
    assertNotNull(repository);
    assertEquals(TestUser.class, repository.getEntityClass());
  }
}
