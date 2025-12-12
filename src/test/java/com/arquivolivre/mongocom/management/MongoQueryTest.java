package com.arquivolivre.mongocom.management;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.bson.Document;
import java.util.Arrays;
import java.util.List;

/** Unit tests for MongoQuery. */
public class MongoQueryTest {

  private MongoQuery query;

  @Before
  public void setUp() {
    query = new MongoQuery();
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull(query);
    assertNotNull(query.getQuery());
    assertTrue(query.getQuery().isEmpty());
  }

  @Test
  public void testConstructorWithFieldAndValue() {
    MongoQuery q = new MongoQuery("name", "John");
    assertNotNull(q.getQuery());
    assertEquals("John", q.getQuery().get("name"));
  }

  @Test
  public void testAddSimpleField() {
    query.add("age", 25);
    assertEquals(25, query.getQuery().get("age"));
  }

  @Test
  public void testAddStringField() {
    query.add("name", "Alice");
    assertEquals("Alice", query.getQuery().get("name"));
  }

  @Test
  public void testAddMultipleFields() {
    query.add("name", "Bob");
    query.add("age", 30);
    query.add("city", "New York");

    Document doc = query.getQuery();
    assertEquals("Bob", doc.get("name"));
    assertEquals(30, doc.get("age"));
    assertEquals("New York", doc.get("city"));
  }

  @Test
  public void testAddNestedMongoQuery() {
    MongoQuery nestedQuery = new MongoQuery("status", "active");
    query.add("$or", nestedQuery);

    assertNotNull(query.getQuery().get("$or"));
  }

  @Test
  public void testAddListOfValues() {
    List<String> values = Arrays.asList("value1", "value2", "value3");
    query.add("tags", values);

    assertNotNull(query.getQuery().get("tags"));
  }

  @Test
  public void testAddListWithNestedQueries() {
    MongoQuery q1 = new MongoQuery("field1", "value1");
    MongoQuery q2 = new MongoQuery("field2", "value2");
    List<MongoQuery> queries = Arrays.asList(q1, q2);

    query.add("$or", queries);
    assertNotNull(query.getQuery().get("$or"));
  }

  @Test
  public void testReturnOnly() {
    query.returnOnly(true, "name", "age", "email");

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("age"));
    assertEquals(1, constraints.get("email"));
  }

  @Test
  public void testReturnOnlyWithoutId() {
    query.returnOnly(false, "name", "age");

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(1, constraints.get("name"));
    assertEquals(1, constraints.get("age"));
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  public void testRemoveFieldsFromResult() {
    query.removeFieldsFromResult("password", "secret");

    Document constraints = query.getConstraints();
    assertNotNull(constraints);
    assertEquals(0, constraints.get("password"));
    assertEquals(0, constraints.get("secret"));
  }

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
    assertNotNull(constraints);
    assertEquals(1, constraints.get("name"));
    assertEquals(0, constraints.get("_id"));
  }

  @Test
  public void testOrderByAscending() {
    query.orderBy("name", MongoQuery.ORDER_ASC);

    Document orderBy = query.getOrderBy();
    assertNotNull(orderBy);
    assertEquals(1, orderBy.get("name"));
  }

  @Test
  public void testOrderByDescending() {
    query.orderBy("age", MongoQuery.ORDER_DESC);

    Document orderBy = query.getOrderBy();
    assertNotNull(orderBy);
    assertEquals(-1, orderBy.get("age"));
  }

  @Test
  public void testLimit() {
    query.limit(10);
    assertEquals(10, query.getLimit());
  }

  @Test
  public void testSkip() {
    query.skip(5);
    assertEquals(5, query.getSkip());
  }

  @Test
  public void testLimitAndSkip() {
    query.limit(20);
    query.skip(10);

    assertEquals(20, query.getLimit());
    assertEquals(10, query.getSkip());
  }

  @Test
  public void testGetQueryJson() {
    query.add("name", "John");
    query.add("age", 30);

    String json = query.getQueryJson();
    assertNotNull(json);
    assertTrue(json.contains("name"));
    assertTrue(json.contains("John"));
  }

  @Test
  public void testGetConstraintsJson() {
    query.returnOnly(true, "name", "age");

    String json = query.getConstraintsJson();
    assertNotNull(json);
    assertTrue(json.contains("name"));
  }

  @Test
  public void testGetConstraintsJsonWhenNull() {
    String json = query.getConstraintsJson();
    assertEquals("{}", json);
  }

  @Test
  public void testGetQueryReturnsNewInstance() {
    query.add("field", "value");
    Document doc1 = query.getQuery();
    Document doc2 = query.getQuery();

    assertNotNull(doc1);
    assertNotNull(doc2);
    assertNotSame(doc1, doc2);
    assertEquals(doc1.toJson(), doc2.toJson());
  }

  @Test
  public void testGetConstraintsReturnsNewInstance() {
    query.returnOnly(true, "field");
    Document doc1 = query.getConstraints();
    Document doc2 = query.getConstraints();

    assertNotNull(doc1);
    assertNotNull(doc2);
    assertNotSame(doc1, doc2);
    assertEquals(doc1.toJson(), doc2.toJson());
  }

  @Test
  public void testGetOrderByReturnsNewInstance() {
    query.orderBy("field", MongoQuery.ORDER_ASC);
    Document doc1 = query.getOrderBy();
    Document doc2 = query.getOrderBy();

    assertNotNull(doc1);
    assertNotNull(doc2);
    assertNotSame(doc1, doc2);
    assertEquals(doc1.toJson(), doc2.toJson());
  }

  @Test
  public void testOrderConstants() {
    assertEquals(1, MongoQuery.ORDER_ASC);
    assertEquals(-1, MongoQuery.ORDER_DESC);
  }

  @Test
  public void testChainedOperations() {
    MongoQuery q = new MongoQuery()
        .add("name", "John")
        .add("age", 30);

    assertEquals("John", q.getQuery().get("name"));
    assertEquals(30, q.getQuery().get("age"));
  }
}