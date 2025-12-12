package com.arquivolivre.mongocom.management;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.annotations.GeneratedValue;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Comprehensive unit tests for CollectionManager using mocked MongoDB connections. */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CollectionManagerWithMocksTest {

  @Mock private MongoClient mockClient;
  @Mock private MongoDatabase mockDatabase;
  @Mock private MongoCollection<org.bson.Document> mockCollection;
  @Mock private FindIterable<org.bson.Document> mockFindIterable;
  @Mock private MongoCursor<org.bson.Document> mockCursor;
  @Mock private InsertOneResult mockInsertResult;
  @Mock private DeleteResult mockDeleteResult;
  @Mock private UpdateResult mockUpdateResult;
  @Mock private MongoIterable<String> mockDbNames;

  private CollectionManager manager;

  // Test entity classes
  @Document(collection = "testCollection")
  public static class TestEntity {
    @Id(autoIncrement = true, generator = IntegerGenerator.class)
    public Integer id;

    public String name;
    public int age;

    public TestEntity() {}

    public TestEntity(Integer id, String name, int age) {
      this.id = id;
      this.name = name;
      this.age = age;
    }
  }

  @Document(collection = "users")
  public static class User {
    @com.arquivolivre.mongocom.annotations.ObjectId
    public String objectId;

    public String username;
    public String email;

    public User() {}

    public User(String username, String email) {
      this.username = username;
      this.email = email;
    }
  }

  @Document(collection = "products")
  public static class Product {
    @Id
    public Integer id;

    @Index(value = "nameIndex", unique = true)
    public String name;

    public double price;

    public Product() {}

    public Product(Integer id, String name, double price) {
      this.id = id;
      this.name = name;
      this.price = price;
    }
  }

  @Before
  public void setUp() {
    // Setup basic mock chain
    when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
    when(mockDatabase.getCollection(anyString())).thenReturn(mockCollection);
    when(mockCollection.find()).thenReturn(mockFindIterable);
    when(mockCollection.find(any(org.bson.Document.class))).thenReturn(mockFindIterable);
    when(mockFindIterable.projection(any())).thenReturn(mockFindIterable);
    when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
    when(mockFindIterable.skip(anyInt())).thenReturn(mockFindIterable);
    when(mockFindIterable.limit(anyInt())).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);

    // Create manager with mocked client
    manager = new CollectionManager(mockClient, "testdb");
  }

  @Test
  public void testUseDatabase() {
    manager.use("newdb");
    verify(mockClient).getDatabase("newdb");
  }

  @Test
  public void testCountWithoutQuery() {
    when(mockCollection.countDocuments(any(org.bson.Document.class))).thenReturn(5L);

    long count = manager.count(TestEntity.class);

    assertEquals(5L, count);
    verify(mockCollection).countDocuments(any(org.bson.Document.class));
  }

  @Test
  public void testCountWithQuery() {
    when(mockCollection.countDocuments(any(org.bson.Document.class))).thenReturn(3L);

    MongoQuery query = new MongoQuery("name", "John");
    long count = manager.count(TestEntity.class, query);

    assertEquals(3L, count);
    verify(mockCollection).countDocuments(any(org.bson.Document.class));
  }

  @Test
  public void testFindAll() {
    org.bson.Document doc1 = new org.bson.Document("id", 1).append("name", "John").append("age", 30);
    org.bson.Document doc2 = new org.bson.Document("id", 2).append("name", "Jane").append("age", 25);

    when(mockCursor.hasNext()).thenReturn(true, true, false);
    when(mockCursor.next()).thenReturn(doc1, doc2);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);

    List<TestEntity> results = manager.find(TestEntity.class);

    assertNotNull(results);
    assertEquals(2, results.size());
    verify(mockCollection).find(any(org.bson.Document.class));
  }

  @Test
  public void testFindWithQuery() {
    org.bson.Document doc = new org.bson.Document("id", 1).append("name", "John").append("age", 30);

    when(mockCursor.hasNext()).thenReturn(true, false);
    when(mockCursor.next()).thenReturn(doc);

    MongoQuery query = new MongoQuery("name", "John");
    List<TestEntity> results = manager.find(TestEntity.class, query);

    assertNotNull(results);
    assertEquals(1, results.size());
    verify(mockCollection).find(any(org.bson.Document.class));
  }

  @Test
  public void testFindWithProjection() {
    org.bson.Document doc = new org.bson.Document("name", "John");

    when(mockCursor.hasNext()).thenReturn(true, false);
    when(mockCursor.next()).thenReturn(doc);

    MongoQuery query = new MongoQuery();
    query.returnOnly(true, "name");
    List<TestEntity> results = manager.find(TestEntity.class, query);

    assertNotNull(results);
    verify(mockFindIterable).projection(any(org.bson.Document.class));
  }

  @Test
  public void testFindWithOrdering() {
    org.bson.Document doc = new org.bson.Document("id", 1).append("name", "John").append("age", 30);

    when(mockCursor.hasNext()).thenReturn(true, false);
    when(mockCursor.next()).thenReturn(doc);

    MongoQuery query = new MongoQuery();
    query.orderBy("name", MongoQuery.ORDER_ASC);
    List<TestEntity> results = manager.find(TestEntity.class, query);

    assertNotNull(results);
    verify(mockFindIterable).sort(any(org.bson.Document.class));
  }

  @Test
  public void testFindWithSkipAndLimit() {
    org.bson.Document doc = new org.bson.Document("id", 1).append("name", "John").append("age", 30);

    when(mockCursor.hasNext()).thenReturn(true, false);
    when(mockCursor.next()).thenReturn(doc);

    MongoQuery query = new MongoQuery();
    query.skip(10);
    query.limit(5);
    List<TestEntity> results = manager.find(TestEntity.class, query);

    assertNotNull(results);
    verify(mockFindIterable).skip(10);
    verify(mockFindIterable).limit(5);
  }

  @Test
  public void testFindOne() {
    org.bson.Document doc = new org.bson.Document("id", 1).append("name", "John").append("age", 30);

    when(mockFindIterable.first()).thenReturn(doc);

    TestEntity result = manager.findOne(TestEntity.class);

    assertNotNull(result);
    assertEquals("John", result.name);
    verify(mockCollection).find();
  }

  @Test
  public void testFindOneWithQuery() {
    org.bson.Document doc = new org.bson.Document("id", 1).append("name", "John").append("age", 30);

    when(mockFindIterable.first()).thenReturn(doc);

    MongoQuery query = new MongoQuery("name", "John");
    TestEntity result = manager.findOne(TestEntity.class, query);

    assertNotNull(result);
    assertEquals("John", result.name);
    verify(mockCollection).find(any(org.bson.Document.class));
  }

  @Test
  public void testFindOneReturnsNull() {
    when(mockFindIterable.first()).thenReturn(null);

    TestEntity result = manager.findOne(TestEntity.class);

    assertNull(result);
  }

  @Test
  public void testFindById() {
    // Use a valid 24-character hex string for ObjectId
    String validObjectId = "507f1f77bcf86cd799439011";
    org.bson.Document doc = new org.bson.Document("_id", validObjectId).append("name", "John");

    when(mockFindIterable.first()).thenReturn(doc);

    TestEntity result = manager.findById(TestEntity.class, validObjectId);

    assertNotNull(result);
    verify(mockCollection).find(any(org.bson.Document.class));
  }

  @Test
  public void testInsert() {
    ObjectId objectId = new ObjectId();
    BsonObjectId bsonObjectId = new BsonObjectId(objectId);

    when(mockInsertResult.getInsertedId()).thenReturn(bsonObjectId);
    when(mockCollection.insertOne(any(org.bson.Document.class))).thenReturn(mockInsertResult);
    when(mockCollection.find()).thenReturn(mockFindIterable);
    when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
    when(mockFindIterable.limit(anyInt())).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(false);

    TestEntity entity = new TestEntity(null, "John", 30);
    String id = manager.insert(entity);

    assertNotNull(id);
    verify(mockCollection, atLeastOnce()).insertOne(any(org.bson.Document.class));
  }

  @Test
  public void testInsertWithNullDocument() {
    String id = manager.insert(null);

    assertNull(id);
    verify(mockCollection, never()).insertOne(any());
  }

  @Test
  public void testInsertWithObjectId() {
    ObjectId objectId = new ObjectId();
    BsonObjectId bsonObjectId = new BsonObjectId(objectId);

    when(mockInsertResult.getInsertedId()).thenReturn(bsonObjectId);
    when(mockCollection.insertOne(any(org.bson.Document.class))).thenReturn(mockInsertResult);

    User user = new User("john", "john@example.com");
    String id = manager.insert(user);

    assertNotNull(id);
    assertNotNull(user.objectId);
    verify(mockCollection).insertOne(any(org.bson.Document.class));
  }

  @Test
  public void testRemove() {
    when(mockCollection.deleteOne(any(Bson.class))).thenReturn(mockDeleteResult);

    TestEntity entity = new TestEntity(1, "John", 30);
    manager.remove(entity);

    verify(mockCollection).deleteOne(any(Bson.class));
  }

  @Test
  public void testUpdate() {
    when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
    when(mockCollection.updateOne(any(org.bson.Document.class), any(org.bson.Document.class)))
        .thenReturn(mockUpdateResult);

    MongoQuery query = new MongoQuery("id", 1);
    TestEntity entity = new TestEntity(1, "John Updated", 31);

    manager.update(query, entity);

    verify(mockCollection).updateOne(any(org.bson.Document.class), any(org.bson.Document.class));
  }

  @Test
  public void testUpdateWithUpsert() {
    when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
    when(mockCollection.updateOne(any(org.bson.Document.class), any(org.bson.Document.class)))
        .thenReturn(mockUpdateResult);

    MongoQuery query = new MongoQuery("id", 1);
    TestEntity entity = new TestEntity(1, "John", 30);

    manager.update(query, entity, true, false);

    verify(mockCollection).updateOne(any(org.bson.Document.class), any(org.bson.Document.class));
  }

  @Test
  public void testUpdateMulti() {
    when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
    when(mockCollection.updateMany(any(org.bson.Document.class), any(org.bson.Document.class)))
        .thenReturn(mockUpdateResult);

    MongoQuery query = new MongoQuery("age", 30);
    TestEntity entity = new TestEntity(null, "Updated", 31);

    manager.updateMulti(query, entity);

    verify(mockCollection).updateMany(any(org.bson.Document.class), any(org.bson.Document.class));
  }

  @Test
  public void testSaveNewDocument() {
    ObjectId objectId = new ObjectId();
    BsonObjectId bsonObjectId = new BsonObjectId(objectId);

    when(mockInsertResult.getInsertedId()).thenReturn(bsonObjectId);
    when(mockCollection.insertOne(any(org.bson.Document.class))).thenReturn(mockInsertResult);
    when(mockCollection.find()).thenReturn(mockFindIterable);
    when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
    when(mockFindIterable.limit(anyInt())).thenReturn(mockFindIterable);
    when(mockFindIterable.iterator()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(false);

    TestEntity entity = new TestEntity(null, "John", 30);
    String id = manager.save(entity);

    assertNotNull(id);
    verify(mockCollection, atLeastOnce()).insertOne(any(org.bson.Document.class));
  }

  @Test
  public void testSaveExistingDocumentWithObjectId() {
    // For save to use replaceOne, the document must have an _id field
    // This happens when using @ObjectId annotation
    ObjectId objectId = new ObjectId();
    BsonObjectId bsonObjectId = new BsonObjectId(objectId);

    when(mockCollection.replaceOne(any(org.bson.conversions.Bson.class),
        any(org.bson.Document.class),
        any(com.mongodb.client.model.ReplaceOptions.class)))
        .thenReturn(mockUpdateResult);

    User user = new User("john", "john@example.com");
    user.objectId = objectId.toString();

    // The save method will call replaceOne if document has _id
    String id = manager.save(user);

    // Verify replaceOne was called
    verify(mockCollection).replaceOne(any(org.bson.conversions.Bson.class),
        any(org.bson.Document.class),
        any(com.mongodb.client.model.ReplaceOptions.class));
  }

  @Test
  public void testSaveWithNullDocument() {
    String id = manager.save(null);

    assertNull(id);
    verify(mockCollection, never()).insertOne(any());
    verify(mockCollection, never()).replaceOne(any(org.bson.conversions.Bson.class),
        any(org.bson.Document.class),
        any(com.mongodb.client.model.ReplaceOptions.class));
  }

  @Test
  public void testGetStatus() {
    org.bson.Document pingResult = new org.bson.Document("ok", 1);
    when(mockDatabase.runCommand(any(org.bson.Document.class))).thenReturn(pingResult);

    // Mock the database names iterator
    @SuppressWarnings("unchecked")
    MongoCursor<String> mockDbCursor = mock(MongoCursor.class);
    when(mockClient.listDatabaseNames()).thenReturn(mockDbNames);
    when(mockDbNames.iterator()).thenReturn(mockDbCursor);
    when(mockDbCursor.hasNext()).thenReturn(true, true, false);
    when(mockDbCursor.next()).thenReturn("testdb", "admin");

    String status = manager.getStatus();

    assertNotNull(status);
    assertTrue(status.contains("connected"));
    verify(mockDatabase).runCommand(any(org.bson.Document.class));
  }

  @Test
  public void testGetStatusWithError() {
    when(mockDatabase.runCommand(any(org.bson.Document.class)))
        .thenThrow(new RuntimeException("Connection error"));

    String status = manager.getStatus();

    assertNotNull(status);
    assertTrue(status.contains("error"));
  }

  @Test
  public void testClose() {
    manager.close();
    verify(mockClient).close();
  }

  @Test
  public void testFindWithEmptyResult() {
    when(mockCursor.hasNext()).thenReturn(false);

    List<TestEntity> results = manager.find(TestEntity.class);

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testCountReturnsZero() {
    when(mockCollection.countDocuments(any(org.bson.Document.class))).thenReturn(0L);

    long count = manager.count(TestEntity.class);

    assertEquals(0L, count);
  }

  @Test
  public void testFindOneWithProjection() {
    org.bson.Document doc = new org.bson.Document("name", "John");

    when(mockFindIterable.first()).thenReturn(doc);

    MongoQuery query = new MongoQuery();
    query.returnOnly(true, "name");
    TestEntity result = manager.findOne(TestEntity.class, query);

    assertNotNull(result);
    verify(mockFindIterable).projection(any(org.bson.Document.class));
  }

  @Test
  public void testUpdateWithWriteConcern() {
    when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
    when(mockCollection.updateOne(any(org.bson.Document.class), any(org.bson.Document.class)))
        .thenReturn(mockUpdateResult);

    MongoQuery query = new MongoQuery("id", 1);
    TestEntity entity = new TestEntity(1, "John", 30);

    manager.update(query, entity, false, false, com.mongodb.WriteConcern.MAJORITY);

    verify(mockCollection).withWriteConcern(any());
    verify(mockCollection).updateOne(any(org.bson.Document.class), any(org.bson.Document.class));
  }

  @Test
  public void testUpdateMultiWithUpsert() {
    when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
    when(mockCollection.updateMany(any(org.bson.Document.class), any(org.bson.Document.class)))
        .thenReturn(mockUpdateResult);

    MongoQuery query = new MongoQuery("status", "active");
    TestEntity entity = new TestEntity(null, "Updated", 0);

    manager.update(query, entity, true, true);

    verify(mockCollection).updateMany(any(org.bson.Document.class), any(org.bson.Document.class));
  }
}