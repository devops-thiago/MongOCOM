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

/** @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br> */
public class QueryPrototype {

  public static void select(String... fields) {}

  public static void from(Class<?> collection) {}

  public static void where(String field) {}

  public static void equalsTo(Object value) {}

  public static void notEqualsTo(Object value) {}

  public static void greaterThan(Object value) {}

  public static void lessThan(Object value) {}

  public static void greaterThanOrEqualTo(Object value) {}

  public static void lessThanOrEqualTo(Object valu) {}

  public static void and(String field) {}

  public static void or(String field) {}

  public static void exists() {}

  public static void notExists() {}

  public static void in(Object... values) {}

  public static void notIn(Object... values) {}
}
