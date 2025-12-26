package com.arquivolivre.mongocom.management;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MongoQuery.
 *
 * <p>Tests query building, field projection, sorting, and pagination.
 */
public class MongoQueryTest {

  private MongoQuery query;

  @Before
  public void setUp() {
    query = new MongoQuery();
  }

  // Constructor tests
  @Test
  public void testDefaultConstructor() {
    MongoQuery q = new MongoQuery();
    assertNotNull(q);
    assertNotNull(q.getQuery());
    assertTrue(q.getQuery().isEmpty());
  }

  @Test
  public void testConstructorWithFieldAndValue() {
    MongoQuery q = new MongoQuery("name", "John");
    assertNotNull(q.getQuery());
    assertEquals("John", q.getQuery().get("name"));
  }

  // add() method tests
  @Test
  public void testAddSimpleField() {
    query.add("name", "John");
    assertEquals("John", query.getQuery().get("name"));
  }

  @Test
  public void testAddMultipleFields() {
    query.add("name", "John");
    query.add("age", 30);
    query.add("active", true);

    Document doc = query.getQuery();
    assertEquals("John", doc.get("name"));
    assertEquals(30, doc.get("age"));
    assertEquals(true, doc.get("active"));
  }

  @Test
  public void testAddWithNestedMongoQuery() {
    MongoQuery nested = new MongoQuery("status", "active");
    query.add("$or", nested);

    Document doc = query.getQuery();
    assertNotNull(doc.get("$or"));
    assertTrue(doc.get("$or") instanceof Document);
  }

  @Test
  public void testAddWithListOfValues() {
    List<String> values = Arrays.asList("value1", "value2", "value3");
    query.add("tags", values);

    Document doc = query.getQuery();
    Object tags = doc.get("tags");
    assertTrue(tags instanceof List);
    assertEquals(3, ((List<?>) tags).size());
  }

  @Test
  public void testAddWithListOfMongoQueries() {
    MongoQuery q1 = new MongoQuery("status", "active");
    MongoQuery q2 = new MongoQuery("age", 30);
    List<MongoQuery> queries = Arrays.asList(q1, q2);

    query.add("$or", queries);

    Document doc = query.getQuery();
    Object orClause = doc.get("$or");
    assertTrue(orClause instanceof List);
    assertEquals(2, ((List<?>) orClause).size());
  }

  @Test
  public void testAddWithIdFieldConvertsToObjectId() {
    String idString = "507f1f77bcf86cd799439011";
    query.add("_id", idString);

    Document doc = query.getQuery();
    Object id = doc.get("_id");
    assertTrue(id instanceof ObjectId);
    assertEquals(idString, ((ObjectId) id).toHexString());
  }

  @Test
  public void testAddReturnsThis() {
    MongoQuery result = query.add("name", "John");
    assertSame(query, result);
  }

  @Test
  public void testAddChaining() {
    query.add("name", "John").add("age", 30).add("active", true);

    Document doc = query.getQuery();
    assertEquals("John", doc.get("name"));
    assertEquals(30, doc.get("age"));
    assertEquals(true, doc.get("active"));
  }

  // returnOnly() tests
  @Test
  public void testReturnOnlyWithIdIncluded() {
    query.returnOnly(true, "name", "email");

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("email"));
    assertNull(constraints.get("_id")); // Not explicitly set when included
  }

  @Test
  public void testReturnOnlyWithIdExcluded() {
    query.returnOnly(false, "name", "email");

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("email"));
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  public void testReturnOnlyWithNoFields() {
    query.returnOnly(true);

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertTrue(constraints.isEmpty());
  }

  @Test
  public void testReturnOnlyOverwritesPreviousConstraints() {
    query.returnOnly(true, "name");
    query.returnOnly(false, "email");

    Document constraints = query.getConstraints();
    assertNull(constraints.get("name")); // Overwritten
    assertEquals(1, constraints.get("email"));
    assertEquals(0, constraints.get("_id"));
  }

  // removeFieldsFromResult() tests
  @Test
  public void testRemoveFieldsFromResult() {
    query.removeFieldsFromResult("password", "ssn");

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(0, constraints.get("password"));
    assertEquals(0, constraints.get("ssn"));
  }

  @Test
  public void testRemoveFieldsFromResultWithNoFields() {
    query.removeFieldsFromResult();

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertTrue(constraints.isEmpty());
  }

  @Test
  public void testRemoveFieldsFromResultOverwritesPreviousConstraints() {
    query.returnOnly(true, "name");
    query.removeFieldsFromResult("password");

    Document constraints = query.getConstraints();
    assertNull(constraints.get("name")); // Overwritten
    assertEquals(0, constraints.get("password"));
  }

  // removeIdFromResult() tests
  @Test
  public void testRemoveIdFromResult() {
    query.removeIdFromResult();

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  public void testRemoveIdFromResultWithExistingConstraints() {
    query.returnOnly(true, "name");
    query.removeIdFromResult();

    Document constraints = query.getConstraints();
    assertEquals(1, constraints.get("name"));
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  public void testRemoveIdFromResultMultipleTimes() {
    query.removeIdFromResult();
    query.removeIdFromResult();

    Document constraints = query.getConstraints();
    assertEquals(0, constraints.get("_id"));
  }

  // orderBy() tests
  @Test
  public void testOrderByAscending() {
    query.orderBy("name", MongoQuery.ORDER_ASC);

    Document orderBy = query.getOrderBy();
    assertNotNull(orderBy);
    assertEquals(1, orderBy.get("name"));
  }

  @Test
  public void testOrderByDescending() {
    query.orderBy("createdAt", MongoQuery.ORDER_DESC);

    Document orderBy = query.getOrderBy();
    assertNotNull(orderBy);
    assertEquals(-1, orderBy.get("createdAt"));
  }

  @Test
  public void testOrderByOverwritesPreviousOrder() {
    query.orderBy("name", MongoQuery.ORDER_ASC);
    query.orderBy("age", MongoQuery.ORDER_DESC);

    Document orderBy = query.getOrderBy();
    assertNull(orderBy.get("name")); // Overwritten
    assertEquals(-1, orderBy.get("age"));
  }

  // limit() and getLimit() tests
  @Test
  public void testLimitDefault() {
    assertEquals(0, query.getLimit());
  }

  @Test
  public void testLimitSet() {
    query.limit(10);
    assertEquals(10, query.getLimit());
  }

  @Test
  public void testLimitOverwrite() {
    query.limit(10);
    query.limit(20);
    assertEquals(20, query.getLimit());
  }

  // skip() and getSkip() tests
  @Test
  public void testSkipDefault() {
    assertEquals(0, query.getSkip());
  }

  @Test
  public void testSkipSet() {
    query.skip(5);
    assertEquals(5, query.getSkip());
  }

  @Test
  public void testSkipOverwrite() {
    query.skip(5);
    query.skip(10);
    assertEquals(10, query.getSkip());
  }

  // getQuery() tests
  @Test
  public void testGetQueryReturnsNewInstance() {
    query.add("name", "John");
    Document doc1 = query.getQuery();
    Document doc2 = query.getQuery();

    assertNotSame(doc1, doc2);
    assertEquals(doc1, doc2);
  }

  @Test
  public void testGetQueryWithEmptyQuery() {
    Document doc = query.getQuery();
    assertNotNull(doc);
    assertTrue(doc.isEmpty());
  }

  // getConstraints() tests
  @Test
  public void testGetConstraintsReturnsNullWhenNotSet() {
    assertNull(query.getConstraints());
  }

  @Test
  public void testGetConstraintsReturnsNewInstance() {
    query.returnOnly(true, "name");
    Document doc1 = query.getConstraints();
    Document doc2 = query.getConstraints();

    assertNotSame(doc1, doc2);
    assertEquals(doc1, doc2);
  }

  // getOrderBy() tests
  @Test
  public void testGetOrderByReturnsNullWhenNotSet() {
    assertNull(query.getOrderBy());
  }

  @Test
  public void testGetOrderByReturnsNewInstance() {
    query.orderBy("name", MongoQuery.ORDER_ASC);
    Document doc1 = query.getOrderBy();
    Document doc2 = query.getOrderBy();

    assertNotSame(doc1, doc2);
    assertEquals(doc1, doc2);
  }

  // JSON conversion tests
  @Test
  public void testGetQueryJson() {
    query.add("name", "John");
    String json = query.getQueryJson();

    assertNotNull(json);
    assertTrue(json.contains("name"));
    assertTrue(json.contains("John"));
  }

  @Test
  public void testGetQueryJsonWithEmptyQuery() {
    String json = query.getQueryJson();
    assertNotNull(json);
    assertEquals("{}", json);
  }

  @Test
  public void testGetConstraintsJson() {
    query.returnOnly(true, "name");
    String json = query.getConstraintsJson();

    assertNotNull(json);
    assertTrue(json.contains("name"));
  }

  @Test
  public void testGetConstraintsJsonWhenNull() {
    String json = query.getConstraintsJson();
    assertEquals("{}", json);
  }

  // Complex query tests
  @Test
  public void testComplexQueryWithAllFeatures() {
    query
        .add("status", "active")
        .add("age", new MongoQuery("$gte", 18))
        .add("tags", Arrays.asList("java", "mongodb"));

    query.returnOnly(false, "name", "email");
    query.orderBy("createdAt", MongoQuery.ORDER_DESC);
    query.limit(10);
    query.skip(20);

    // Verify query
    Document queryDoc = query.getQuery();
    assertEquals("active", queryDoc.get("status"));
    assertNotNull(queryDoc.get("age"));
    assertNotNull(queryDoc.get("tags"));

    // Verify constraints
    Document constraints = query.getConstraints();
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("email"));
    assertEquals(0, constraints.get("_id"));

    // Verify order
    Document orderBy = query.getOrderBy();
    assertEquals(-1, orderBy.get("createdAt"));

    // Verify pagination
    assertEquals(10, query.getLimit());
    assertEquals(20, query.getSkip());
  }

  // Constants tests
  @Test
  public void testOrderConstants() {
    assertEquals(1, MongoQuery.ORDER_ASC);
    assertEquals(-1, MongoQuery.ORDER_DESC);
  }

  // Edge cases
  @Test
  public void testAddWithNullValue() {
    query.add("field", null);
    Document doc = query.getQuery();
    assertTrue(doc.containsKey("field"));
    assertNull(doc.get("field"));
  }

  @Test
  public void testAddWithEmptyString() {
    query.add("field", "");
    assertEquals("", query.getQuery().get("field"));
  }

  @Test
  public void testAddWithNumericTypes() {
    query.add("intField", 42);
    query.add("longField", 42L);
    query.add("doubleField", 42.5);
    query.add("floatField", 42.5f);

    Document doc = query.getQuery();
    assertEquals(42, doc.get("intField"));
    assertEquals(42L, doc.get("longField"));
    assertEquals(42.5, doc.get("doubleField"));
    assertEquals(42.5f, doc.get("floatField"));
  }

  @Test
  public void testLimitWithZero() {
    query.limit(0);
    assertEquals(0, query.getLimit());
  }

  @Test
  public void testSkipWithZero() {
    query.skip(0);
    assertEquals(0, query.getSkip());
  }

  @Test
  public void testLimitWithNegativeValue() {
    query.limit(-1);
    assertEquals(-1, query.getLimit());
  }

  @Test
  public void testSkipWithNegativeValue() {
    query.skip(-1);
    assertEquals(-1, query.getSkip());
  }
}
