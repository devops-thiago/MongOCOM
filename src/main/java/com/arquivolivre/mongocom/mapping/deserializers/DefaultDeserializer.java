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

import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import com.arquivolivre.mongocom.mapping.FieldDeserializer;
import java.util.Date;

/**
 * Default fallback deserializer for simple types.
 *
 * <p>This deserializer handles common simple types that don't require special processing:
 *
 * <ul>
 *   <li>String
 *   <li>Date
 *   <li>Any type where direct assignment is possible
 * </ul>
 *
 * <p>This is the last deserializer in the chain and acts as a catch-all for types not handled by
 * more specific deserializers.
 *
 * <p><b>Priority:</b> 40 (lowest - fallback handler)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class DefaultDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 40;

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    // This is the fallback deserializer - it can handle anything
    return true;
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Object value = context.getValue();
    final Class<?> fieldType = context.getFieldType();

    // Direct assignment if types are compatible
    if (fieldType.isInstance(value)) {
      context.setFieldValue(value);
    } else if (String.class.equals(fieldType)) {
      // Handle String conversion
      context.setFieldValue(value.toString());
    } else if (Date.class.equals(fieldType) && value instanceof Date) {
      // Handle Date (common MongoDB type)
      context.setFieldValue(value);
    } else {
      // Try direct assignment and let it fail if incompatible
      try {
        context.setFieldValue(value);
      } catch (final IllegalArgumentException e) {
        throw new MappingException(
            "Cannot assign value of type "
                + (value != null ? value.getClass().getName() : "null")
                + " to field "
                + context.getFieldName()
                + " of type "
                + fieldType.getName(),
            e);
      }
    }
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "DefaultDeserializer{priority=" + PRIORITY + ", role=FALLBACK}";
  }
}
