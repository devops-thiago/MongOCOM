/*
 * Copyright 2014 Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br&gt;.
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

package com.arquivolivre.mongocom.utils;

/**
 * QueryPrototype provides a fluent interface prototype for MongoDB queries.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br>
 */
public class QueryPrototype {

  /**
   * Select specific fields to return in query results.
   *
   * @param fields the field names to select
   */
  public static void select(String... fields) {}

  /**
   * Specify the collection to query from.
   *
   * @param collection the collection class
   */
  public static void from(Class<?> collection) {}

  /**
   * Specify a field to filter on.
   *
   * @param field the field name
   */
  public static void where(String field) {}

  /**
   * Filter for documents where the field equals the specified value.
   *
   * @param value the value to match
   */
  public static void equalsTo(Object value) {}

  /**
   * Filter for documents where the field does not equal the specified value.
   *
   * @param value the value to not match
   */
  public static void notEqualsTo(Object value) {}

  /**
   * Filter for documents where the field is greater than the specified value.
   *
   * @param value the comparison value
   */
  public static void greaterThan(Object value) {}

  /**
   * Filter for documents where the field is less than the specified value.
   *
   * @param value the comparison value
   */
  public static void lessThan(Object value) {}

  /**
   * Filter for documents where the field is greater than or equal to the specified value.
   *
   * @param value the comparison value
   */
  public static void greaterThanOrEqualTo(Object value) {}

  /**
   * Filter for documents where the field is less than or equal to the specified value.
   *
   * @param value the comparison value
   */
  public static void lessThanOrEqualTo(Object value) {}

  /**
   * Add an AND condition with the specified field.
   *
   * @param field the field name
   */
  public static void and(String field) {}

  /**
   * Add an OR condition with the specified field.
   *
   * @param field the field name
   */
  public static void or(String field) {}

  /** Filter for documents where the field exists. */
  public static void exists() {}

  /** Filter for documents where the field does not exist. */
  public static void notExists() {}

  /**
   * Filter for documents where the field value is in the specified array of values.
   *
   * @param values the array of values to match
   */
  public static void in(Object... values) {}

  /**
   * Filter for documents where the field value is not in the specified array of values.
   *
   * @param values the array of values to not match
   */
  public static void notIn(Object... values) {}
}
