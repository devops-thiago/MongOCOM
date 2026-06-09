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

import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import com.arquivolivre.mongocom.mapping.FieldDeserializer;

/**
 * Deserializer for fields marked with @ObjectId annotation.
 *
 * <p>This deserializer handles the MongoDB "_id" field, converting it to a String representation
 * for fields annotated with @ObjectId.
 *
 * <p><b>Priority:</b> 10 (annotation-based, must run before generic deserializers)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class ObjectIdDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 10;
  private static final String MONGO_ID_FIELD = "_id";

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    return context.hasAnnotation(ObjectId.class);
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    // Get _id from document
    final Object idValue = context.getFromDocument(MONGO_ID_FIELD);

    if (idValue == null) {
      // No _id in document, skip
      return;
    }

    // Convert to String
    final String idString = idValue.toString();

    // Verify field type is String
    if (!String.class.equals(context.getFieldType())) {
      throw new MappingException(
          "@ObjectId field must be of type String, but found: "
              + context.getFieldType().getName()
              + " for field: "
              + context.getFieldName());
    }

    context.setFieldValue(idString);
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "ObjectIdDeserializer{priority=" + PRIORITY + "}";
  }
}
