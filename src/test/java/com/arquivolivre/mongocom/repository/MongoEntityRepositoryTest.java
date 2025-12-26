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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import com.arquivolivre.mongocom.management.MongoQuery;
import com.arquivolivre.mongocom.testutil.TestEntities;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Comprehensive unit tests for MongoEntityRepository.
 *
 * <p>Tests all CRUD operations, serialization/deserialization, and error handling using mocks to
 * avoid MongoDB dependencies.
 *
 * <p>Coverage target: 90%+ of MongoEntityRepository class
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoEntityRepositoryTest {

  @Mock private MongoConnectionManager mockConnectionManager;
  @Mock private MongoDatabase mockDatabase;
  @Mock private MongoCollection<Document> mockCollection;
  @Mock private FindIterable<Document> mockFindIterable;
  @Mock private MongoCursor<Document> mockCursor;
  @Mock private UpdateResult mockUpdateResult;
  @Mock private DeleteResult mockDeleteResult;

  private MongoEntityRepository<TestEntities.TestUser, String> repository;
  private MongoEntityRepository<TestEntities.EntityWithObjectId, String> simpleRepository;

  @Before
  public void setUp() {
    // Setup mock chain: connectionManager -> database -> collection
    when(mockConnectionManager.getDefaultDatabase()).thenReturn(mockDatabase);
    when(mockDatabase.getCollection(anyString())).thenReturn(mockCollection);

    // Create repository instance
    repository = new MongoEntityRepository<>(TestEntities.TestUser.class, mockConnectionManager);
    simpleRepository =
        new MongoEntityRepository<>(TestEntities.EntityWithObjectId.class, mockConnectionManager);
  }

  // ==================== Constructor Tests ====================

  @Test
  public void testConstructor_WithValidParameters_ShouldCreateRepository() {
    assertNotNull(repository);
    assertEquals(TestEntities.TestUser.class, repository.getEntityClass());
    assertEquals("users", repository.getCollectionName());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_WithNullEntityClass_ShouldThrowException() {
    new MongoEntityRepository<>(null, mockConnectionManager);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_WithNullConnectionManager_ShouldThrowException() {
    new MongoEntityRepository<>(TestEntities.TestUser.class, null);
  }

  @Test
  public void testGetEntityClass_ShouldReturnCorrectClass() {
    assertEquals(TestEntities.TestUser.class, repository.getEntityClass());
  }

  @Test
  public void testGetCollectionName_ShouldReturnCorrectName() {
    assertEquals("users", repository.getCollectionName());
  }

  @Test
  public void testToString_ShouldReturnDescriptiveString() {
    final String result = repository.toString();
    assertTrue(result.contains("TestUser"));
    assertTrue(result.contains("users"));
  }

  // ==================== Count Tests ====================

  @Test
  public void testCount_WithNoDocuments_ShouldReturnZero() {
    when(mockCollection.countDocuments()).thenReturn(0L);

    final long count = repository.count();

    assertEquals(0L, count);
    verify(mockCollection).countDocuments();
  }

  @Test
  public void testCount_WithMultipleDocuments_ShouldReturnCorrectCount() {
    when(mockCollection.countDocuments()).thenReturn(42L);

    final long count = repository.count();

    assertEquals(42L, count);
    verify(mockCollection).countDocuments();
  }

  @Test
  public void testCountWithQuery_WithValidQuery_ShouldReturnCount() {
    final MongoQuery query = new MongoQuery().add("age", 25);
    when(mockCollection.countDocuments(any(Bson.class))).thenReturn(5L);

    final long count = repository.count(query);

    assertEquals(5L, count);
    verify(mockCollection).countDocuments(any(Bson.class));
  }

  @Test(expected = NullPointerException.class)
  public void testCountWithQuery_WithNullQuery_ShouldThrowException() {
    repository.count(null);
  }

  // ==================== FindAll Tests ====================

  @Test
  public void testFindAll_WithNoDocuments_ShouldReturnEmptyList() {
    when(mockCollection.find()).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(false);

    final List<TestEntities.TestUser> results = repository.findAll();

    assertNotNull(results);
    assertTrue(results.isEmpty());
    verify(mockCollection).find();
  }

  @Test
  public void testFindAll_WithMultipleDocuments_ShouldReturnAllEntities() {
    final Document doc1 =
        new Document("username", "user1")
            .append("email", "user1@test.com")
            .append("age", 25)
            .append("active", true);
    final Document doc2 =
        new Document("username", "user2")
            .append("email", "user2@test.com")
            .append("age", 30)
            .append("active", false);

    when(mockCollection.find()).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true, true, false);
    when(mockCursor.next()).thenReturn(doc1, doc2);

    final List<TestEntities.TestUser> results = repository.findAll();

    assertNotNull(results);
    assertEquals(2, results.size());
    assertEquals("user1", results.get(0).getUsername());
    assertEquals("user2", results.get(1).getUsername());
    verify(mockCollection).find();
  }

  // ==================== Find with Query Tests ====================

  @Test
  public void testFind_WithValidQuery_ShouldReturnMatchingEntities() {
    final MongoQuery query = new MongoQuery().add("age", 25);
    final Document doc =
        new Document("username", "user1")
            .append("email", "user1@test.com")
            .append("age", 25)
            .append("active", true);

    when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true, false);
    when(mockCursor.next()).thenReturn(doc);

    final List<TestEntities.TestUser> results = repository.find(query);

    assertNotNull(results);
    assertEquals(1, results.size());
    assertEquals("user1", results.get(0).getUsername());
    verify(mockCollection).find(any(Bson.class));
  }

  @Test(expected = NullPointerException.class)
  public void testFind_WithNullQuery_ShouldThrowException() {
    repository.find(null);
  }

  @Test
  public void testFind_WithNoMatches_ShouldReturnEmptyList() {
    final MongoQuery query = new MongoQuery().add("age", 999);

    when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(false);

    final List<TestEntities.TestUser> results = repository.find(query);

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  // ==================== FindOne Tests ====================

  @Test
  public void testFindOne_WithMatchingDocument_ShouldReturnOptionalWithEntity() {
    final MongoQuery query = new MongoQuery().add("username", "user1");
    final Document doc =
        new Document("username", "user1")
            .append("email", "user1@test.com")
            .append("age", 25)
            .append("active", true);

    when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.first()).thenReturn(doc);

    final Optional<TestEntities.TestUser> result = repository.findOne(query);

    assertTrue(result.isPresent());
    assertEquals("user1", result.get().getUsername());
    verify(mockCollection).find(any(Bson.class));
  }

  @Test
  public void testFindOne_WithNoMatch_ShouldReturnEmptyOptional() {
    final MongoQuery query = new MongoQuery().add("username", "nonexistent");

    when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.first()).thenReturn(null);

    final Optional<TestEntities.TestUser> result = repository.findOne(query);

    assertFalse(result.isPresent());
  }

  @Test(expected = NullPointerException.class)
  public void testFindOne_WithNullQuery_ShouldThrowException() {
    repository.findOne(null);
  }

  // ==================== FindById Tests ====================

  @Test
  public void testFindById_WithExistingId_ShouldReturnOptionalWithEntity() {
    final String id = "user123";
    final Document doc =
        new Document("_id", id)
            .append("username", "user1")
            .append("email", "user1@test.com")
            .append("age", 25)
            .append("active", true);

    when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.first()).thenReturn(doc);

    final Optional<TestEntities.TestUser> result = repository.findById(id);

    assertTrue(result.isPresent());
    assertEquals("user1", result.get().getUsername());
    verify(mockCollection).find(any(Document.class));
  }

  @Test
  public void testFindById_WithNonExistingId_ShouldReturnEmptyOptional() {
    final String id = "nonexistent";

    when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.first()).thenReturn(null);

    final Optional<TestEntities.TestUser> result = repository.findById(id);

    assertFalse(result.isPresent());
  }

  @Test(expected = NullPointerException.class)
  public void testFindById_WithNullId_ShouldThrowException() {
    repository.findById(null);
  }

  // ==================== ExistsById Tests ====================

  @Test
  public void testExistsById_WithExistingId_ShouldReturnTrue() {
    final String id = "user123";
    when(mockCollection.countDocuments(any(Document.class))).thenReturn(1L);

    final boolean exists = repository.existsById(id);

    assertTrue(exists);
    verify(mockCollection).countDocuments(any(Document.class));
  }

  @Test
  public void testExistsById_WithNonExistingId_ShouldReturnFalse() {
    final String id = "nonexistent";
    when(mockCollection.countDocuments(any(Document.class))).thenReturn(0L);

    final boolean exists = repository.existsById(id);

    assertFalse(exists);
  }

  @Test(expected = NullPointerException.class)
  public void testExistsById_WithNullId_ShouldThrowException() {
    repository.existsById(null);
  }

  // ==================== Insert Tests ====================

  @Test
  public void testInsert_WithValidEntity_ShouldInsertAndReturnId() {
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "user1@test.com", 25);

    doAnswer(
            invocation -> {
              Document doc = invocation.getArgument(0);
              doc.put("_id", "generated-id-123");
              return null;
            })
        .when(mockCollection)
        .insertOne(any(Document.class));

    final String id = repository.insert(user);

    assertNotNull(id);
    assertEquals("generated-id-123", id);
    verify(mockCollection).insertOne(any(Document.class));
  }

  @Test(expected = NullPointerException.class)
  public void testInsert_WithNullEntity_ShouldThrowException() {
    repository.insert(null);
  }

  // ==================== Update Tests ====================

  @Test
  public void testUpdate_WithValidQueryAndEntity_ShouldUpdateAndReturnCount() {
    final MongoQuery query = new MongoQuery().add("username", "user1");
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "newemail@test.com", 26);

    when(mockCollection.updateOne(any(Bson.class), any(Document.class)))
        .thenReturn(mockUpdateResult);
    when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

    final long count = repository.update(query, user);

    assertEquals(1L, count);
    verify(mockCollection).updateOne(any(Bson.class), any(Document.class));
  }

  @Test
  public void testUpdate_WithNoMatches_ShouldReturnZero() {
    final MongoQuery query = new MongoQuery().add("username", "nonexistent");
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "email@test.com", 25);

    when(mockCollection.updateOne(any(Bson.class), any(Document.class)))
        .thenReturn(mockUpdateResult);
    when(mockUpdateResult.getModifiedCount()).thenReturn(0L);

    final long count = repository.update(query, user);

    assertEquals(0L, count);
  }

  @Test(expected = NullPointerException.class)
  public void testUpdate_WithNullQuery_ShouldThrowException() {
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "email@test.com", 25);
    repository.update(null, user);
  }

  @Test(expected = NullPointerException.class)
  public void testUpdate_WithNullEntity_ShouldThrowException() {
    final MongoQuery query = new MongoQuery().add("username", "user1");
    repository.update(query, null);
  }

  // ==================== UpdateMulti Tests ====================

  @Test
  public void testUpdateMulti_WithValidQueryAndEntity_ShouldUpdateMultipleAndReturnCount() {
    final MongoQuery query = new MongoQuery().add("age", 25);
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "email@test.com", 26);

    when(mockCollection.updateMany(any(Bson.class), any(Document.class)))
        .thenReturn(mockUpdateResult);
    when(mockUpdateResult.getModifiedCount()).thenReturn(5L);

    final long count = repository.updateMulti(query, user);

    assertEquals(5L, count);
    verify(mockCollection).updateMany(any(Bson.class), any(Document.class));
  }

  @Test(expected = NullPointerException.class)
  public void testUpdateMulti_WithNullQuery_ShouldThrowException() {
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "email@test.com", 25);
    repository.updateMulti(null, user);
  }

  @Test(expected = NullPointerException.class)
  public void testUpdateMulti_WithNullEntity_ShouldThrowException() {
    final MongoQuery query = new MongoQuery().add("age", 25);
    repository.updateMulti(query, null);
  }

  // ==================== Save Tests ====================

  @Test
  public void testSave_WithNewEntity_ShouldInsert() {
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "user1@test.com", 25);

    doAnswer(
            invocation -> {
              Document doc = invocation.getArgument(0);
              doc.put("_id", "new-id-123");
              return null;
            })
        .when(mockCollection)
        .insertOne(any(Document.class));

    final String id = repository.save(user);

    assertNotNull(id);
    verify(mockCollection).insertOne(any(Document.class));
  }

  @Test
  public void testSave_WithEntityWithoutIdField_ShouldAlwaysInsert() {
    // EntityWithObjectId has no @Id field, so save() always inserts
    final TestEntities.EntityWithObjectId entity = new TestEntities.EntityWithObjectId();
    entity.setName("Test Entity");

    doAnswer(
            invocation -> {
              Document doc = invocation.getArgument(0);
              doc.put("_id", "507f1f77bcf86cd799439011");
              return null;
            })
        .when(mockCollection)
        .insertOne(any(Document.class));

    final String id = simpleRepository.save(entity);

    assertEquals("507f1f77bcf86cd799439011", id);
    verify(mockCollection).insertOne(any(Document.class));
  }

  @Test
  public void testSave_WithEntityHavingNullId_ShouldInsert() {
    // TestUser with null username (@Id field) should insert
    final TestEntities.TestUser user = new TestEntities.TestUser();
    user.setUsername(null);
    user.setEmail("test@test.com");
    user.setAge(30);

    doAnswer(
            invocation -> {
              Document doc = invocation.getArgument(0);
              doc.put("_id", "generated-id");
              return null;
            })
        .when(mockCollection)
        .insertOne(any(Document.class));

    final String id = repository.save(user);

    assertNotNull(id);
    verify(mockCollection).insertOne(any(Document.class));
    verify(mockCollection, never()).updateOne(any(Bson.class), any(Document.class));
  }

  @Test(expected = NullPointerException.class)
  public void testSave_WithNullEntity_ShouldThrowException() {
    repository.save(null);
  }

  // ==================== Delete Tests ====================

  @Test
  public void testDelete_WithEntityHavingId_ShouldDeleteAndReturnTrue() {
    // TestUser has @Id on username field
    final TestEntities.TestUser user = new TestEntities.TestUser("user1", "user1@test.com", 25);

    when(mockCollection.deleteOne(any(Document.class))).thenReturn(mockDeleteResult);
    when(mockDeleteResult.getDeletedCount()).thenReturn(1L);

    final boolean deleted = repository.delete(user);

    assertTrue(deleted);
    verify(mockCollection).deleteOne(any(Document.class));
  }

  @Test
  public void testDelete_WithEntityHavingNullId_ShouldReturnFalse() {
    // Create user with null username (@Id field)
    final TestEntities.TestUser user = new TestEntities.TestUser();
    user.setUsername(null);
    user.setEmail("test@test.com");
    user.setAge(25);

    // The delete method returns false early when ID is null, no MongoDB call
    final boolean deleted = repository.delete(user);

    assertFalse(deleted);
    verify(mockCollection, never()).deleteOne(any(Document.class));
  }

  @Test(expected = NullPointerException.class)
  public void testDelete_WithNullEntity_ShouldThrowException() {
    repository.delete((TestEntities.TestUser) null);
  }

  @Test
  public void testDeleteWithQuery_WithValidQuery_ShouldDeleteAndReturnCount() {
    final MongoQuery query = new MongoQuery().add("age", 25);

    when(mockCollection.deleteMany(any(Bson.class))).thenReturn(mockDeleteResult);
    when(mockDeleteResult.getDeletedCount()).thenReturn(3L);

    final long count = repository.delete(query);

    assertEquals(3L, count);
    verify(mockCollection).deleteMany(any(Bson.class));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteWithQuery_WithNullQuery_ShouldThrowException() {
    repository.delete((MongoQuery) null);
  }

  // ==================== DeleteById Tests ====================

  @Test
  public void testDeleteById_WithExistingId_ShouldDeleteAndReturnTrue() {
    final String id = "user-id-123";

    when(mockCollection.deleteOne(any(Document.class))).thenReturn(mockDeleteResult);
    when(mockDeleteResult.getDeletedCount()).thenReturn(1L);

    final boolean deleted = repository.deleteById(id);

    assertTrue(deleted);
    verify(mockCollection).deleteOne(any(Document.class));
  }

  @Test
  public void testDeleteById_WithNonExistingId_ShouldReturnFalse() {
    final String id = "nonexistent";

    when(mockCollection.deleteOne(any(Document.class))).thenReturn(mockDeleteResult);
    when(mockDeleteResult.getDeletedCount()).thenReturn(0L);

    final boolean deleted = repository.deleteById(id);

    assertFalse(deleted);
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteById_WithNullId_ShouldThrowException() {
    repository.deleteById(null);
  }

  // ==================== DeleteAll Tests ====================

  @Test
  public void testDeleteAll_WithMultipleDocuments_ShouldDeleteAllAndReturnCount() {
    when(mockCollection.deleteMany(any(Document.class))).thenReturn(mockDeleteResult);
    when(mockDeleteResult.getDeletedCount()).thenReturn(10L);

    final long count = repository.deleteAll();

    assertEquals(10L, count);
    verify(mockCollection).deleteMany(any(Document.class));
  }

  @Test
  public void testDeleteAll_WithNoDocuments_ShouldReturnZero() {
    when(mockCollection.deleteMany(any(Document.class))).thenReturn(mockDeleteResult);
    when(mockDeleteResult.getDeletedCount()).thenReturn(0L);

    final long count = repository.deleteAll();

    assertEquals(0L, count);
  }
}
