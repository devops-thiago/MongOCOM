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

package com.arquivolivre.mongocom.mapping;

/**
 * Strategy interface for field deserialization.
 *
 * <p>Implementations handle specific types of field deserialization (primitives, enums, lists,
 * references, etc.) following the Strategy pattern.
 *
 * <p><b>Design Pattern:</b> Strategy - defines a family of deserialization algorithms.
 *
 * <p><b>Design Pattern:</b> Chain of Responsibility - deserializers are chained by priority.
 *
 * <p><b>Thread Safety:</b> Implementations must be thread-safe as they may be shared across
 * threads.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public interface FieldDeserializer {

  /**
   * Check if this deserializer can handle the given field.
   *
   * <p>This method is called by the chain to determine which deserializer should process the field.
   *
   * @param context the deserialization context (never null)
   * @return true if this deserializer can handle the field
   */
  boolean canHandle(FieldDeserializationContext context);

  /**
   * Deserialize the field value and set it on the target object.
   *
   * <p>This method should only be called if {@link #canHandle(FieldDeserializationContext)} returns
   * true.
   *
   * @param context the deserialization context (never null)
   * @throws Exception if deserialization fails
   */
  void deserialize(FieldDeserializationContext context) throws Exception;

  /**
   * Get the priority of this deserializer.
   *
   * <p>Lower numbers = higher priority. Deserializers are executed in priority order.
   *
   * <p>Recommended priorities:
   *
   * <ul>
   *   <li>0-9: Special cases (null values, internal fields)
   *   <li>10-19: Annotations (@Reference, @ObjectId)
   *   <li>20-29: Complex types (Lists, nested objects)
   *   <li>30-39: Simple types (primitives, enums, strings)
   *   <li>40+: Default/fallback handlers
   * </ul>
   *
   * @return the priority (lower = higher priority)
   */
  int getPriority();
}
