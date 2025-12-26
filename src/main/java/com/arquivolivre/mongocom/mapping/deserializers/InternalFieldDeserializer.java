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

package com.arquivolivre.mongocom.mapping.deserializers;

import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import com.arquivolivre.mongocom.mapping.FieldDeserializer;

/**
 * Deserializer for fields marked with @Internal annotation.
 *
 * <p>This deserializer handles @Internal fields by deserializing complex objects from embedded
 * subdocuments or primitive values directly. The @Internal annotation indicates that the field is
 * embedded directly in the parent document rather than stored as a reference.
 *
 * <p><b>Priority:</b> 5 (high - must run before NestedObjectDeserializer)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 3
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class InternalFieldDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 5;
  private final NestedObjectDeserializer nestedDeserializer = new NestedObjectDeserializer();
  private final PrimitiveDeserializer primitiveDeserializer = new PrimitiveDeserializer();

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    return context.hasAnnotation(Internal.class);
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Object value = context.getValue();

    if (value == null) {
      context.setFieldValue(null);
      return;
    }

    // Check if it's a primitive or simple type
    if (isPrimitiveOrSimple(value)) {
      // Deserialize primitive values directly
      primitiveDeserializer.deserialize(context);
    } else {
      // Delegate to NestedObjectDeserializer to deserialize complex objects
      nestedDeserializer.deserialize(context);
    }
  }

  /**
   * Check if a value is a primitive or simple type that should be deserialized directly.
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
    return "InternalFieldDeserializer{priority=" + PRIORITY + "}";
  }
}
