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

/**
 * Deserializer for null values.
 *
 * <p>This deserializer handles null values from documents. It prevents setting null on primitive
 * fields (which would cause IllegalArgumentException) and allows null for object fields.
 *
 * <p><b>Priority:</b> 0 (highest - must run first to prevent NPE in other deserializers)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class NullValueDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 0;

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    return context.isValueNull();
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    if (context.isFieldPrimitive()) {
      throw new MappingException(
          "Cannot set null value on primitive field: "
              + context.getFieldName()
              + " of type "
              + context.getFieldType().getName());
    }

    // Set null on object field
    context.setFieldValue(null);
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "NullValueDeserializer{priority=" + PRIORITY + "}";
  }
}
