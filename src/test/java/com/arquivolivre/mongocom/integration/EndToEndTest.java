package com.arquivolivre.mongocom.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.arquivolivre.mongocom.management.MongoQuery;
import com.arquivolivre.mongocom.testutil.TestEntities.TestAddress;
import com.arquivolivre.mongocom.testutil.TestEntities.TestCompany;
import com.arquivolivre.mongocom.testutil.TestEntities.TestUser;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end integration tests using real MongoDB.
 *
 * <p>Tests complete workflows including:
 *
 * <ul>
 *   <li>CRUD operations
 *   <li>Reference handling
 *   <li>Index creation
 *   <li>Concurrent access
 *   <li>Complex queries
 * </ul>
 *
 * @author MongOCOM Team
 * @since 0.4-SNAPSHOT
 */
@DisplayName("End-to-End Integration Tests")
class EndToEndTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should perform complete CRUD workflow")
  void shouldPerformCompleteCrudWorkflow() {
    // Create
    final TestUser user = new TestUser("john_doe", "john@example.com", 30);
    user.setTags(Arrays.asList("developer", "java", "mongodb"));

    final TestAddress address = new TestAddress("123 Main St", "New York", "10001");
    user.setAddress(address);

    collectionManager.insert(user);
    assertNotNull(user.getId(), "User ID should be generated");

    // Read
    final MongoQuery findQuery = new MongoQuery();
    findQuery.add("username", "john_doe");
    final List<TestUser> foundUsers = collectionManager.find(TestUser.class, findQuery);

    assertEquals(1, foundUsers.size(), "Should find exactly one user");
    final TestUser foundUser = foundUsers.get(0);
    assertEquals("john_doe", foundUser.getUsername());
    assertEquals("john@example.com", foundUser.getEmail());
    assertEquals(30, foundUser.getAge());
    assertEquals(3, foundUser.getTags().size());
    assertNotNull(foundUser.getAddress());
    assertEquals("New York", foundUser.getAddress().getCity());

    // Update
    foundUser.setAge(31);
    foundUser.setEmail("john.doe@example.com");
    final MongoQuery updateQuery = new MongoQuery();
    updateQuery.add("username", "john_doe");
    collectionManager.update(updateQuery, foundUser);

    final List<TestUser> updatedUsers = collectionManager.find(TestUser.class, findQuery);
    assertEquals(1, updatedUsers.size());
    assertEquals(31, updatedUsers.get(0).getAge());
    assertEquals("john.doe@example.com", updatedUsers.get(0).getEmail());

    // Delete
    collectionManager.remove(foundUser);
    final List<TestUser> afterDelete = collectionManager.find(TestUser.class, findQuery);
    assertTrue(afterDelete.isEmpty(), "User should be deleted");
  }

  @Test
  @DisplayName("Should handle references correctly")
  void shouldHandleReferences() {
    // Create company first
    final TestCompany company = new TestCompany("Tech Corp", 500);
    collectionManager.insert(company);
    assertNotNull(company.getId(), "Company ID should be generated");

    // Create user with company reference
    final TestUser user = new TestUser("jane_doe", "jane@example.com", 28);
    user.setCompany(company);
    collectionManager.insert(user);
    assertNotNull(user.getId(), "User ID should be generated");

    // Retrieve user and verify reference
    final MongoQuery query = new MongoQuery();
    query.add("username", "jane_doe");
    final List<TestUser> users = collectionManager.find(TestUser.class, query);

    assertEquals(1, users.size());
    final TestUser retrievedUser = users.get(0);
    assertNotNull(retrievedUser.getCompany(), "Company reference should be loaded");
    assertEquals("Tech Corp", retrievedUser.getCompany().getName());
    assertEquals(500, retrievedUser.getCompany().getEmployeeCount());
  }

  @Test
  @DisplayName("Should create and use indexes")
  void shouldCreateAndUseIndexes() {
    // Insert multiple users
    for (int i = 0; i < 10; i++) {
      final TestUser user = new TestUser("user" + i, "user" + i + "@example.com", 20 + i);
      collectionManager.insert(user);
    }

    // Query by indexed email field
    final MongoQuery query = new MongoQuery();
    query.add("email", "user5@example.com");
    final List<TestUser> users = collectionManager.find(TestUser.class, query);

    assertEquals(1, users.size());
    assertEquals("user5", users.get(0).getUsername());
    assertEquals(25, users.get(0).getAge());
  }

  @Test
  @DisplayName("Should handle concurrent access safely")
  void shouldHandleConcurrentAccess() throws InterruptedException {
    final int threadCount = 10;
    final int operationsPerThread = 5;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);
    final AtomicInteger successCount = new AtomicInteger(0);
    final AtomicInteger errorCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                final TestUser user =
                    new TestUser(
                        "thread" + threadId + "_user" + j,
                        "thread" + threadId + "_user" + j + "@example.com",
                        20 + j);
                collectionManager.insert(user);
                successCount.incrementAndGet();
              }
            } catch (Exception e) {
              errorCount.incrementAndGet();
            } finally {
              latch.countDown();
            }
          });
    }

    assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete");
    executor.shutdown();

    assertEquals(
        threadCount * operationsPerThread, successCount.get(), "All operations should succeed");
    assertEquals(0, errorCount.get(), "No errors should occur");

    // Verify all users were inserted
    final List<TestUser> allUsers = collectionManager.find(TestUser.class, new MongoQuery());
    assertEquals(threadCount * operationsPerThread, allUsers.size());
  }

  @Test
  @DisplayName("Should handle complex queries")
  void shouldHandleComplexQueries() {
    // Insert test data
    for (int i = 0; i < 20; i++) {
      final TestUser user = new TestUser("user" + i, "user" + i + "@example.com", 20 + (i % 10));
      user.setActive(i % 2 == 0);
      collectionManager.insert(user);
    }

    // Query: active users aged 25 or older
    final MongoQuery query = new MongoQuery();
    query.add("active", true);
    query.add("age", new MongoQuery().add("$gte", 25));

    final List<TestUser> results = collectionManager.find(TestUser.class, query);

    assertFalse(results.isEmpty(), "Should find matching users");
    for (TestUser user : results) {
      assertTrue(user.isActive(), "All results should be active");
      assertTrue(user.getAge() >= 25, "All results should be 25 or older");
    }
  }

  @Test
  @DisplayName("Should handle embedded documents")
  void shouldHandleEmbeddedDocuments() {
    final TestUser user = new TestUser("embedded_test", "embedded@example.com", 35);
    final TestAddress address = new TestAddress("456 Oak Ave", "San Francisco", "94102");
    user.setAddress(address);

    collectionManager.insert(user);

    final MongoQuery query = new MongoQuery();
    query.add("username", "embedded_test");
    final List<TestUser> users = collectionManager.find(TestUser.class, query);

    assertEquals(1, users.size());
    final TestUser retrievedUser = users.get(0);
    assertNotNull(retrievedUser.getAddress(), "Address should be embedded");
    assertEquals("456 Oak Ave", retrievedUser.getAddress().getStreet());
    assertEquals("San Francisco", retrievedUser.getAddress().getCity());
    assertEquals("94102", retrievedUser.getAddress().getZipCode());
  }

  @Test
  @DisplayName("Should handle list fields")
  void shouldHandleListFields() {
    final TestUser user = new TestUser("list_test", "list@example.com", 40);
    user.setTags(Arrays.asList("tag1", "tag2", "tag3", "tag4"));

    collectionManager.insert(user);

    final MongoQuery query = new MongoQuery();
    query.add("username", "list_test");
    final List<TestUser> users = collectionManager.find(TestUser.class, query);

    assertEquals(1, users.size());
    final TestUser retrievedUser = users.get(0);
    assertNotNull(retrievedUser.getTags(), "Tags should be present");
    assertEquals(4, retrievedUser.getTags().size());
    assertTrue(retrievedUser.getTags().contains("tag1"));
    assertTrue(retrievedUser.getTags().contains("tag4"));
  }

  @Test
  @DisplayName("Should handle update operations")
  void shouldHandleUpdateOperations() {
    // Insert initial user
    final TestUser user = new TestUser("update_test", "update@example.com", 25);
    collectionManager.insert(user);

    // Update multiple fields
    user.setAge(26);
    user.setEmail("updated@example.com");
    user.setActive(false);
    final MongoQuery updateQuery = new MongoQuery();
    updateQuery.add("username", "update_test");
    collectionManager.update(updateQuery, user);

    // Verify updates
    final MongoQuery query = new MongoQuery();
    query.add("username", "update_test");
    final List<TestUser> users = collectionManager.find(TestUser.class, query);

    assertEquals(1, users.size());
    final TestUser updated = users.get(0);
    assertEquals(26, updated.getAge());
    assertEquals("updated@example.com", updated.getEmail());
    assertFalse(updated.isActive());
  }

  @Test
  @DisplayName("Should handle batch operations")
  void shouldHandleBatchOperations() {
    // Insert batch
    final int batchSize = 100;
    for (int i = 0; i < batchSize; i++) {
      final TestUser user =
          new TestUser("batch_user" + i, "batch" + i + "@example.com", 20 + (i % 50));
      collectionManager.insert(user);
    }

    // Verify count
    final List<TestUser> allUsers = collectionManager.find(TestUser.class, new MongoQuery());
    assertEquals(batchSize, allUsers.size(), "All batch users should be inserted");

    // Query subset
    final MongoQuery ageQuery = new MongoQuery();
    ageQuery.add("age", new MongoQuery().add("$lt", 30));
    final List<TestUser> youngUsers = collectionManager.find(TestUser.class, ageQuery);

    assertFalse(youngUsers.isEmpty(), "Should find users under 30");
    for (TestUser user : youngUsers) {
      assertTrue(user.getAge() < 30, "All results should be under 30");
    }
  }

  @Test
  @DisplayName("Should handle null values correctly")
  void shouldHandleNullValues() {
    final TestUser user = new TestUser("null_test", "null@example.com", 30);
    // Don't set address, tags, or company - they should be null
    collectionManager.insert(user);

    final MongoQuery query = new MongoQuery();
    query.add("username", "null_test");
    final List<TestUser> users = collectionManager.find(TestUser.class, query);

    assertEquals(1, users.size());
    final TestUser retrieved = users.get(0);
    assertNull(retrieved.getAddress(), "Address should be null");
    assertNull(retrieved.getTags(), "Tags should be null");
    assertNull(retrieved.getCompany(), "Company should be null");
  }
}
