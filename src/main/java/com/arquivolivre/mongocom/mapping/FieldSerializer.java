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

import com.arquivolivre.mongocom.exception.MappingException;

/**
 * Strategy interface for field serialization.
 *
 * <p>Implementations of this interface handle serialization of specific field types or fields with
 * specific annotations. Each serializer is responsible for determining if it can handle a field and
 * then performing the serialization.
 *
 * <p><b>Design Pattern:</b> Strategy Pattern + Chain of Responsibility - each serializer is a
 * strategy that can be chained together.
 *
 * <p><b>Thread Safety:</b> Implementations must be stateless and thread-safe as they may be shared
 * across multiple threads.
 *
 * <p><b>Complexity:</b> Each implementation should have low cyclomatic complexity (target: 3-5).
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public interface FieldSerializer {

  /**
   * Check if this serializer can handle the field.
   *
   * <p>This method should be fast and have no side effects. It should only examine the context to
   * determine if this serializer is appropriate for the field.
   *
   * @param context the serialization context (never null)
   * @return true if this serializer can handle the field, false otherwise
   */
  boolean canHandle(FieldSerializationContext context);

  /**
   * Serialize the field value into the document.
   *
   * <p>This method is only called if {@link #canHandle(FieldSerializationContext)} returns true.
   * The implementation should serialize the field value and put it into the document using the
   * context's {@link FieldSerializationContext#putInDocument(Object)} method.
   *
   * <p><b>Complexity Target:</b> Keep this method simple (cyclomatic complexity ≤ 5).
   *
   * @param context the serialization context (never null)
   * @throws MappingException if serialization fails
   */
  void serialize(FieldSerializationContext context);

  /**
   * Get serializer priority (lower = higher priority).
   *
   * <p>Serializers are executed in priority order. Lower numbers are executed first. This allows
   * control over the order in which serializers are tried.
   *
   * <p>Recommended priority ranges:
   *
   * <ul>
   *   <li>1-10: Critical serializers (null checks, etc.)
   *   <li>11-50: Annotation-based serializers (@Id, @ObjectId, etc.)
   *   <li>51-100: Type-based serializers (List, Enum, etc.)
   *   <li>1000+: Default/fallback serializers
   * </ul>
   *
   * @return priority value (default: 100)
   */
  default int getPriority() {
    return 100;
  }
}
