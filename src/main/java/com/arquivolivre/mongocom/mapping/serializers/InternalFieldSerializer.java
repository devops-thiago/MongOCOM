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

package com.arquivolivre.mongocom.mapping.serializers;

import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.mapping.FieldSerializer;

/**
 * Serializer for fields marked with @Internal annotation.
 *
 * <p>This serializer handles @Internal fields by embedding complex objects as subdocuments or
 * storing primitive values directly. The @Internal annotation indicates that the field should be
 * embedded directly in the parent document rather than stored as a reference.
 *
 * <p><b>Priority:</b> 5 (high - must run before NestedObjectSerializer)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 3
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class InternalFieldSerializer implements FieldSerializer {

  private static final int PRIORITY = 5;
  private final NestedObjectSerializer nestedSerializer = new NestedObjectSerializer();
  private final PrimitiveSerializer primitiveSerializer = new PrimitiveSerializer();

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return context.hasAnnotation(Internal.class);
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    final Object value = context.getValue();

    if (value == null) {
      context.putInDocument(null);
      return;
    }

    // Check if it's a primitive or simple type
    if (isPrimitiveOrSimple(value)) {
      // Store primitive values directly
      primitiveSerializer.serialize(context);
    } else {
      // Delegate to NestedObjectSerializer to embed complex objects
      nestedSerializer.serialize(context);
    }
  }

  /**
   * Check if a value is a primitive or simple type that should be stored directly.
   *
   * @param value the value to check
   * @return true if primitive or simple type
   */
  private boolean isPrimitiveOrSimple(final Object value) {
    final Class<?> type = value.getClass();
    return type.isPrimitive()
        || type == String.class
        || type == Boolean.class
        || type == Integer.class
        || type == Long.class
        || type == Double.class
        || type == Float.class
        || type == Short.class
        || type == Byte.class
        || type == Character.class
        || Number.class.isAssignableFrom(type);
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "InternalFieldSerializer{priority=" + PRIORITY + "}";
  }
}
