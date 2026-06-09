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

import com.arquivolivre.mongocom.management.MongoQuery;
import java.util.Objects;

/**
 * Fluent API for building MongoDB queries.
 *
 * <p>This class provides a more intuitive and type-safe way to build queries compared to the
 * original MongoQuery class. It uses the Builder pattern to create queries step-by-step.
 *
 * <p><b>Design Pattern:</b> Builder - constructs complex queries step-by-step
 *
 * <p><b>Design Pattern:</b> Fluent Interface - method chaining for readability
 *
 * <p><b>Example Usage:</b>
 *
 * <pre>{@code
 * MongoQuery query = QueryBuilder.create()
 *     .where("name").equals("John")
 *     .and("age").greaterThan(18)
 *     .orderBy("name", QueryBuilder.Order.ASC)
 *     .limit(10)
 *     .build();
 * }</pre>
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class QueryBuilder {

  /** Sort order enumeration. */
  public enum Order {
    /** Ascending order (1). */
    ASC(1),
    /** Descending order (-1). */
    DESC(-1);

    private final int value;

    Order(final int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  private final MongoQuery query;
  private String currentField;

  /** Private constructor for builder pattern. */
  private QueryBuilder() {
    this.query = new MongoQuery();
  }

  /**
   * Create a new query builder.
   *
   * @return new query builder instance
   */
  public static QueryBuilder create() {
    return new QueryBuilder();
  }

  /**
   * Start a condition for a field.
   *
   * @param field the field name (must not be null)
   * @return this builder for chaining
   * @throws NullPointerException if field is null
   */
  public QueryBuilder where(final String field) {
    Objects.requireNonNull(field, "Field cannot be null");
    this.currentField = field;
    return this;
  }

  /**
   * Continue with another condition (alias for where).
   *
   * @param field the field name (must not be null)
   * @return this builder for chaining
   * @throws NullPointerException if field is null
   */
  public QueryBuilder and(final String field) {
    return where(field);
  }

  /**
   * Add equals condition.
   *
   * @param value the value to match
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder isEqualTo(final Object value) {
    ensureFieldSet();
    query.add(currentField, value);
    return this;
  }

  /**
   * Add not equals condition.
   *
   * @param value the value to not match
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder notEquals(final Object value) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$ne", value));
    return this;
  }

  /**
   * Add greater than condition.
   *
   * @param value the value to compare
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder greaterThan(final Object value) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$gt", value));
    return this;
  }

  /**
   * Add greater than or equals condition.
   *
   * @param value the value to compare
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder greaterThanOrEquals(final Object value) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$gte", value));
    return this;
  }

  /**
   * Add less than condition.
   *
   * @param value the value to compare
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder lessThan(final Object value) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$lt", value));
    return this;
  }

  /**
   * Add less than or equals condition.
   *
   * @param value the value to compare
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder lessThanOrEquals(final Object value) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$lte", value));
    return this;
  }

  /**
   * Add in condition (value in array).
   *
   * @param values the array of values
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder isIn(final Object... values) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$in", java.util.Arrays.asList(values)));
    return this;
  }

  /**
   * Add not in condition (value not in array).
   *
   * @param values the array of values
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder notIn(final Object... values) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$nin", java.util.Arrays.asList(values)));
    return this;
  }

  /**
   * Add exists condition.
   *
   * @param exists true if field should exist, false otherwise
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder exists(final boolean exists) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$exists", exists));
    return this;
  }

  /**
   * Add regex pattern matching condition.
   *
   * @param pattern the regex pattern
   * @return this builder for chaining
   * @throws IllegalStateException if no field specified
   */
  public QueryBuilder regex(final String pattern) {
    ensureFieldSet();
    query.add(currentField, new MongoQuery("$regex", pattern));
    return this;
  }

  /**
   * Set sort order for a field.
   *
   * @param field the field to sort by
   * @param order the sort order
   * @return this builder for chaining
   */
  public QueryBuilder orderBy(final String field, final Order order) {
    Objects.requireNonNull(field, "Field cannot be null");
    Objects.requireNonNull(order, "Order cannot be null");
    query.orderBy(field, order.getValue());
    return this;
  }

  /**
   * Set maximum number of results.
   *
   * @param limit the maximum number of results
   * @return this builder for chaining
   * @throws IllegalArgumentException if limit is negative
   */
  public QueryBuilder limit(final int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("Limit cannot be negative");
    }
    query.limit(limit);
    return this;
  }

  /**
   * Set number of results to skip.
   *
   * @param skip the number of results to skip
   * @return this builder for chaining
   * @throws IllegalArgumentException if skip is negative
   */
  public QueryBuilder skip(final int skip) {
    if (skip < 0) {
      throw new IllegalArgumentException("Skip cannot be negative");
    }
    query.skip(skip);
    return this;
  }

  /**
   * Specify fields to return (projection).
   *
   * @param includeId whether to include _id field
   * @param fields the fields to return
   * @return this builder for chaining
   */
  public QueryBuilder returnOnly(final boolean includeId, final String... fields) {
    query.returnOnly(includeId, fields);
    return this;
  }

  /**
   * Specify fields to exclude from results.
   *
   * @param fields the fields to exclude
   * @return this builder for chaining
   */
  public QueryBuilder exclude(final String... fields) {
    query.removeFieldsFromResult(fields);
    return this;
  }

  /**
   * Build the final MongoQuery.
   *
   * @return the constructed MongoQuery
   */
  public MongoQuery build() {
    return query;
  }

  /**
   * Ensure a field has been set before adding a condition.
   *
   * @throws IllegalStateException if no field is set
   */
  private void ensureFieldSet() {
    if (currentField == null) {
      throw new IllegalStateException("No field specified. Call where() or and() first.");
    }
  }

  @Override
  public String toString() {
    return "QueryBuilder{query=" + query.getQueryJson() + '}';
  }
}
