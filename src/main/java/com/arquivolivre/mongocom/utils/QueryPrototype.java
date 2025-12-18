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

package com.arquivolivre.mongocom.utils;

/**
 * Prototype class for query building (currently not implemented).
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 */
public class QueryPrototype {

  public static void select(final String... fields) {}

  public static void from(final Class<?> collection) {}

  public static void where(final String field) {}

  public static void equalsTo(final Object value) {}

  public static void notEqualsTo(final Object value) {}

  public static void greaterThan(final Object value) {}

  public static void lessThan(final Object value) {}

  public static void greaterThanOrEqualTo(final Object value) {}

  public static void lessThanOrEqualTo(final Object value) {}

  public static void and(final String field) {}

  public static void or(final String field) {}

  public static void exists() {}

  public static void notExists() {}

  public static void in(final Object... values) {}

  public static void notIn(final Object... values) {}
}
