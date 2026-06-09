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

import com.arquivolivre.mongocom.management.MongoQuery;
import org.junit.Test;

/**
 * Unit tests for QueryBuilder.
 *
 * <p>Tests the fluent API for building MongoDB queries.
 */
public class QueryBuilderTest {

  @Test
  public void testCreate_ShouldReturnNewBuilder() {
    final QueryBuilder builder = QueryBuilder.create();
    assertNotNull(builder);
  }

  @Test
  public void testWhere_ShouldSetCurrentField() {
    final QueryBuilder builder = QueryBuilder.create();
    final QueryBuilder result = builder.where("name");
    assertSame("Should return same builder for chaining", builder, result);
  }

  @Test(expected = NullPointerException.class)
  public void testWhere_WithNullField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.where(null);
  }

  @Test
  public void testAnd_ShouldSetCurrentField() {
    final QueryBuilder builder = QueryBuilder.create();
    final QueryBuilder result = builder.and("age");
    assertSame("Should return same builder for chaining", builder, result);
  }

  @Test(expected = NullPointerException.class)
  public void testAnd_WithNullField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.and(null);
  }

  @Test
  public void testIsEqualTo_ShouldAddEqualsCondition() {
    final MongoQuery query = QueryBuilder.create().where("name").isEqualTo("John").build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("name"));
    assertTrue("Query should contain value", json.contains("John"));
  }

  @Test(expected = IllegalStateException.class)
  public void testIsEqualTo_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.isEqualTo("value");
  }

  @Test
  public void testNotEquals_ShouldAddNotEqualsCondition() {
    final MongoQuery query = QueryBuilder.create().where("status").notEquals("inactive").build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("status"));
    assertTrue("Query should contain $ne operator", json.contains("$ne"));
  }

  @Test(expected = IllegalStateException.class)
  public void testNotEquals_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.notEquals("value");
  }

  @Test
  public void testGreaterThan_ShouldAddGreaterThanCondition() {
    final MongoQuery query = QueryBuilder.create().where("age").greaterThan(18).build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("age"));
    assertTrue("Query should contain $gt operator", json.contains("$gt"));
  }

  @Test(expected = IllegalStateException.class)
  public void testGreaterThan_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.greaterThan(10);
  }

  @Test
  public void testGreaterThanOrEquals_ShouldAddCondition() {
    final MongoQuery query = QueryBuilder.create().where("score").greaterThanOrEquals(50).build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("score"));
    assertTrue("Query should contain $gte operator", json.contains("$gte"));
  }

  @Test(expected = IllegalStateException.class)
  public void testGreaterThanOrEquals_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.greaterThanOrEquals(10);
  }

  @Test
  public void testLessThan_ShouldAddLessThanCondition() {
    final MongoQuery query = QueryBuilder.create().where("price").lessThan(100.0).build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("price"));
    assertTrue("Query should contain $lt operator", json.contains("$lt"));
  }

  @Test(expected = IllegalStateException.class)
  public void testLessThan_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.lessThan(10);
  }

  @Test
  public void testLessThanOrEquals_ShouldAddCondition() {
    final MongoQuery query = QueryBuilder.create().where("quantity").lessThanOrEquals(10).build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("quantity"));
    assertTrue("Query should contain $lte operator", json.contains("$lte"));
  }

  @Test(expected = IllegalStateException.class)
  public void testLessThanOrEquals_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.lessThanOrEquals(10);
  }

  @Test
  public void testIn_ShouldAddInCondition() {
    final MongoQuery query =
        QueryBuilder.create().where("category").isIn("electronics", "books", "toys").build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("category"));
    assertTrue("Query should contain $in operator", json.contains("$in"));
  }

  @Test(expected = IllegalStateException.class)
  public void testIn_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.isIn("value1", "value2");
  }

  @Test
  public void testNotIn_ShouldAddNotInCondition() {
    final MongoQuery query =
        QueryBuilder.create().where("status").notIn("deleted", "archived").build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("status"));
    assertTrue("Query should contain $nin operator", json.contains("$nin"));
  }

  @Test(expected = IllegalStateException.class)
  public void testNotIn_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.notIn("value1", "value2");
  }

  @Test
  public void testExists_WithTrue_ShouldAddExistsCondition() {
    final MongoQuery query = QueryBuilder.create().where("email").exists(true).build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("email"));
    assertTrue("Query should contain $exists operator", json.contains("$exists"));
  }

  @Test
  public void testExists_WithFalse_ShouldAddExistsCondition() {
    final MongoQuery query = QueryBuilder.create().where("deletedAt").exists(false).build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("deletedAt"));
    assertTrue("Query should contain $exists operator", json.contains("$exists"));
  }

  @Test(expected = IllegalStateException.class)
  public void testExists_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.exists(true);
  }

  @Test
  public void testRegex_ShouldAddRegexCondition() {
    final MongoQuery query = QueryBuilder.create().where("name").regex("^John.*").build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain field name", json.contains("name"));
    assertTrue("Query should contain $regex operator", json.contains("$regex"));
  }

  @Test(expected = IllegalStateException.class)
  public void testRegex_WithoutField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.regex("pattern");
  }

  @Test
  public void testOrderBy_WithAscending_ShouldAddSortOrder() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("name")
            .isEqualTo("John")
            .orderBy("age", QueryBuilder.Order.ASC)
            .build();

    assertNotNull(query);
  }

  @Test
  public void testOrderBy_WithDescending_ShouldAddSortOrder() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("name")
            .isEqualTo("John")
            .orderBy("createdAt", QueryBuilder.Order.DESC)
            .build();

    assertNotNull(query);
  }

  @Test(expected = NullPointerException.class)
  public void testOrderBy_WithNullField_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.orderBy(null, QueryBuilder.Order.ASC);
  }

  @Test(expected = NullPointerException.class)
  public void testOrderBy_WithNullOrder_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.orderBy("name", null);
  }

  @Test
  public void testLimit_WithPositiveValue_ShouldSetLimit() {
    final MongoQuery query =
        QueryBuilder.create().where("status").isEqualTo("active").limit(10).build();

    assertNotNull(query);
  }

  @Test
  public void testLimit_WithZero_ShouldSetLimit() {
    final MongoQuery query =
        QueryBuilder.create().where("status").isEqualTo("active").limit(0).build();

    assertNotNull(query);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLimit_WithNegativeValue_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.limit(-1);
  }

  @Test
  public void testSkip_WithPositiveValue_ShouldSetSkip() {
    final MongoQuery query =
        QueryBuilder.create().where("status").isEqualTo("active").skip(20).build();

    assertNotNull(query);
  }

  @Test
  public void testSkip_WithZero_ShouldSetSkip() {
    final MongoQuery query =
        QueryBuilder.create().where("status").isEqualTo("active").skip(0).build();

    assertNotNull(query);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSkip_WithNegativeValue_ShouldThrowException() {
    final QueryBuilder builder = QueryBuilder.create();
    builder.skip(-1);
  }

  @Test
  public void testReturnOnly_WithIncludeId_ShouldSetProjection() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("status")
            .isEqualTo("active")
            .returnOnly(true, "name", "email")
            .build();

    assertNotNull(query);
  }

  @Test
  public void testReturnOnly_WithoutIncludeId_ShouldSetProjection() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("status")
            .isEqualTo("active")
            .returnOnly(false, "name", "email")
            .build();

    assertNotNull(query);
  }

  @Test
  public void testExclude_ShouldSetExclusionProjection() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("status")
            .isEqualTo("active")
            .exclude("password", "secretKey")
            .build();

    assertNotNull(query);
  }

  @Test
  public void testChaining_MultipleConditions_ShouldBuildComplexQuery() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("name")
            .isEqualTo("John")
            .and("age")
            .greaterThan(18)
            .and("status")
            .notEquals("inactive")
            .orderBy("createdAt", QueryBuilder.Order.DESC)
            .limit(10)
            .skip(5)
            .build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain name field", json.contains("name"));
    assertTrue("Query should contain age field", json.contains("age"));
    assertTrue("Query should contain status field", json.contains("status"));
  }

  @Test
  public void testChaining_WithProjection_ShouldBuildComplexQuery() {
    final MongoQuery query =
        QueryBuilder.create()
            .where("category")
            .isIn("electronics", "books")
            .and("price")
            .lessThan(100.0)
            .returnOnly(true, "name", "price", "category")
            .orderBy("price", QueryBuilder.Order.ASC)
            .limit(20)
            .build();

    assertNotNull(query);
    final String json = query.getQueryJson();
    assertTrue("Query should contain category field", json.contains("category"));
    assertTrue("Query should contain price field", json.contains("price"));
  }

  @Test
  public void testToString_ShouldReturnQueryRepresentation() {
    final QueryBuilder builder = QueryBuilder.create().where("name").isEqualTo("John");

    final String str = builder.toString();
    assertNotNull(str);
    assertTrue("Should contain class name", str.contains("QueryBuilder"));
  }

  @Test
  public void testOrderEnum_AscendingValue() {
    assertEquals(1, QueryBuilder.Order.ASC.getValue());
  }

  @Test
  public void testOrderEnum_DescendingValue() {
    assertEquals(-1, QueryBuilder.Order.DESC.getValue());
  }

  @Test
  public void testBuild_EmptyQuery_ShouldReturnValidQuery() {
    final MongoQuery query = QueryBuilder.create().build();
    assertNotNull(query);
  }

  @Test
  public void testBuild_MultipleBuilds_ShouldReturnSameQuery() {
    final QueryBuilder builder = QueryBuilder.create().where("name").isEqualTo("John");

    final MongoQuery query1 = builder.build();
    final MongoQuery query2 = builder.build();

    assertSame("Should return same query instance", query1, query2);
  }
}
