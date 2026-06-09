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

import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.mapping.FieldSerializer;

/**
 * Serializer for fields marked with @ObjectId annotation.
 *
 * <p>This serializer handles the MongoDB "_id" field, storing the value with the key "_id" instead
 * of the field name. This ensures MongoDB queries by _id work correctly.
 *
 * <p><b>Priority:</b> 10 (annotation-based, must run before generic serializers)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class ObjectIdSerializer implements FieldSerializer {

  private static final int PRIORITY = 10;
  private static final String MONGODB_ID_FIELD = "_id";

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return context.hasAnnotation(ObjectId.class);
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    final Object value = context.getValue();

    if (value != null) {
      // Store with "_id" key instead of field name
      context.putInDocument(MONGODB_ID_FIELD, value);
    }
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "ObjectIdSerializer{priority=" + PRIORITY + "}";
  }
}
