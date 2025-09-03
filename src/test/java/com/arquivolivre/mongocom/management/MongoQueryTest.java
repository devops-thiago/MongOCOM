package com.arquivolivre.mongocom.management;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MongoQuery Tests")
class MongoQueryTest {

  private MongoQuery mongoQuery;

  @BeforeEach
  void setUp() {
    mongoQuery = new MongoQuery();
  }

  @Test
  @DisplayName("Should create empty query with default constructor")
  void testDefaultConstructor() {
    assertNotNull(mongoQuery.getQuery());
    assertTrue(mongoQuery.getQuery().isEmpty());
    assertEquals(0, mongoQuery.getLimit());
    assertEquals(0, mongoQuery.getSkip());
  }

  @Test
  @DisplayName("Should create query with field and value")
  void testConstructorWithFieldAndValue() {
    MongoQuery query = new MongoQuery("name", "John");

    Document expectedQuery = new Document("name", "John");
    assertEquals(expectedQuery, query.getQuery());
  }

  @Test
  @DisplayName("Should add simple field-value pair to query")
  void testAddSimpleFieldValue() {
    MongoQuery result = mongoQuery.add("age", 25);

    assertSame(mongoQuery, result); // Should return same instance for chaining
    assertEquals(25, mongoQuery.getQuery().get("age"));
  }

  @Test
  @DisplayName("Should handle ObjectId for _id field")
  void testAddObjectIdField() {
    String objectIdString = "507f1f77bcf86cd799439011";
    mongoQuery.add("_id", objectIdString);

    Object idValue = mongoQuery.getQuery().get("_id");
    assertInstanceOf(ObjectId.class, idValue);
    assertEquals(objectIdString, ((ObjectId) idValue).toHexString());
  }

  @Test
  @DisplayName("Should add nested MongoQuery")
  void testAddNestedMongoQuery() {
    MongoQuery nestedQuery = new MongoQuery("status", "active");
    mongoQuery.add("$and", nestedQuery);

    Document queryDoc = mongoQuery.getQuery();
    assertTrue(queryDoc.containsKey("$and"));
    assertEquals(nestedQuery.getQuery(), queryDoc.get("$and"));
  }

  @Test
  @DisplayName("Should handle list with MongoQuery objects")
  void testAddListWithMongoQuery() {
    MongoQuery query1 = new MongoQuery("status", "active");
    MongoQuery query2 = new MongoQuery("type", "user");
    List<Object> queryList = Arrays.asList(query1, "simpleValue", query2);

    mongoQuery.add("$or", queryList);

    Document queryDoc = mongoQuery.getQuery();
    List<Object> resultList = (List<Object>) queryDoc.get("$or");

    assertEquals(3, resultList.size());
    assertEquals(query1.getQuery(), resultList.get(0));
    assertEquals("simpleValue", resultList.get(1));
    assertEquals(query2.getQuery(), resultList.get(2));
  }

  @Test
  @DisplayName("Should handle list with mixed objects")
  void testAddListWithMixedObjects() {
    List<Object> mixedList = Arrays.asList("string", 42, true);
    mongoQuery.add("mixedField", mixedList);

    assertEquals(mixedList, mongoQuery.getQuery().get("mixedField"));
  }

  @Test
  @DisplayName("Should set returnOnly constraints with ID")
  void testReturnOnlyWithId() {
    mongoQuery.returnOnly(true, "name", "email");

    Document constraints = mongoQuery.getConstraints();
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("email"));
    assertFalse(constraints.containsKey("_id")); // Should not explicitly exclude _id
  }

  @Test
  @DisplayName("Should set returnOnly constraints without ID")
  void testReturnOnlyWithoutId() {
    mongoQuery.returnOnly(false, "name", "email");

    Document constraints = mongoQuery.getConstraints();
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("email"));
    assertEquals(0, constraints.get("_id")); // Should explicitly exclude _id
  }

  @Test
  @DisplayName("Should remove fields from result")
  void testRemoveFieldsFromResult() {
    mongoQuery.removeFieldsFromResult("password", "internalId");

    Document constraints = mongoQuery.getConstraints();
    assertEquals(0, constraints.get("password"));
    assertEquals(0, constraints.get("internalId"));
  }

  @Test
  @DisplayName("Should remove ID from result")
  void testRemoveIdFromResult() {
    mongoQuery.removeIdFromResult();

    Document constraints = mongoQuery.getConstraints();
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  @DisplayName("Should remove ID from result when constraints already exist")
  void testRemoveIdFromResultWithExistingConstraints() {
    mongoQuery.returnOnly(true, "name");
    mongoQuery.removeIdFromResult();

    Document constraints = mongoQuery.getConstraints();
    assertEquals(1, constraints.get("name"));
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  @DisplayName("Should set order by ascending")
  void testOrderByAscending() {
    mongoQuery.orderBy("createdAt", MongoQuery.ORDER_ASC);

    Document orderBy = mongoQuery.getOrderBy();
    assertEquals(1, orderBy.get("createdAt"));
  }

  @Test
  @DisplayName("Should set order by descending")
  void testOrderByDescending() {
    mongoQuery.orderBy("createdAt", MongoQuery.ORDER_DESC);

    Document orderBy = mongoQuery.getOrderBy();
    assertEquals(-1, orderBy.get("createdAt"));
  }

  @Test
  @DisplayName("Should set limit")
  void testSetLimit() {
    mongoQuery.limit(10);
    assertEquals(10, mongoQuery.getLimit());
  }

  @Test
  @DisplayName("Should set skip")
  void testSetSkip() {
    mongoQuery.skip(5);
    assertEquals(5, mongoQuery.getSkip());
  }

  @Test
  @DisplayName("Should return query as JSON")
  void testGetQueryJson() {
    mongoQuery.add("name", "John").add("age", 25);

    String json = mongoQuery.getQueryJson();
    assertNotNull(json);
    assertTrue(json.contains("name"));
    assertTrue(json.contains("John"));
    assertTrue(json.contains("age"));
  }

  @Test
  @DisplayName("Should return constraints as JSON")
  void testGetConstraintsJson() {
    mongoQuery.returnOnly(false, "name", "email");

    String json = mongoQuery.getConstraintsJson();
    assertNotNull(json);
    assertTrue(json.contains("name"));
    assertTrue(json.contains("email"));
    assertTrue(json.contains("_id"));
  }

  @Test
  @DisplayName("Should return empty JSON when constraints are null")
  void testGetConstraintsJsonWhenNull() {
    String json = mongoQuery.getConstraintsJson();
    assertEquals("{}", json);
  }

  @Test
  @DisplayName("Should return defensive copies of internal documents")
  void testDefensiveCopies() {
    mongoQuery.add("name", "John");
    mongoQuery.returnOnly(true, "name");
    mongoQuery.orderBy("name", MongoQuery.ORDER_ASC);

    Document query = mongoQuery.getQuery();
    Document constraints = mongoQuery.getConstraints();
    Document orderBy = mongoQuery.getOrderBy();

    // Modify returned documents
    query.put("modified", true);
    constraints.put("modified", true);
    orderBy.put("modified", true);

    // Original should not be modified
    assertFalse(mongoQuery.getQuery().containsKey("modified"));
    assertFalse(mongoQuery.getConstraints().containsKey("modified"));
    assertFalse(mongoQuery.getOrderBy().containsKey("modified"));
  }

  @Test
  @DisplayName("Should handle null constraints in getConstraints")
  void testGetConstraintsWhenNull() {
    Document constraints = mongoQuery.getConstraints();
    assertNull(constraints);
  }

  @Test
  @DisplayName("Should handle null orderBy in getOrderBy")
  void testGetOrderByWhenNull() {
    Document orderBy = mongoQuery.getOrderBy();
    assertNull(orderBy);
  }

  @Test
  @DisplayName("Should support method chaining")
  void testMethodChaining() {
    MongoQuery result = mongoQuery.add("name", "John").add("age", 25);

    assertSame(mongoQuery, result);
    assertEquals("John", mongoQuery.getQuery().get("name"));
    assertEquals(25, mongoQuery.getQuery().get("age"));
  }

  @Test
  @DisplayName("Should handle complex query building")
  void testComplexQueryBuilding() {
    MongoQuery subQuery1 = new MongoQuery("status", "active");
    MongoQuery subQuery2 = new MongoQuery("role", "admin");

    mongoQuery.add("$or", Arrays.asList(subQuery1, subQuery2)).add("department", "IT");

    mongoQuery.returnOnly(false, "name", "email", "department");
    mongoQuery.orderBy("name", MongoQuery.ORDER_ASC);
    mongoQuery.limit(20);
    mongoQuery.skip(10);

    // Verify query structure
    Document query = mongoQuery.getQuery();
    assertTrue(query.containsKey("$or"));
    assertEquals("IT", query.get("department"));

    // Verify constraints
    Document constraints = mongoQuery.getConstraints();
    assertEquals(1, constraints.get("name"));
    assertEquals(0, constraints.get("_id"));

    // Verify ordering and pagination
    assertEquals(1, mongoQuery.getOrderBy().get("name"));
    assertEquals(20, mongoQuery.getLimit());
    assertEquals(10, mongoQuery.getSkip());
  }
}
