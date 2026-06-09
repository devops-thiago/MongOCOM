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
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.mapping.FieldSerializer;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializer for fields marked with @Reference annotation.
 *
 * <p>This serializer handles referenced entities by storing only their ObjectId instead of the
 * entire object, preventing data duplication and enabling lazy loading.
 *
 * <p>The serializer looks for a field marked with @ObjectId in the referenced entity and stores
 * that value. If no @ObjectId field is found, it logs a warning and stores null.
 *
 * <p><b>Priority:</b> 15 (annotation-based, must run before generic serializers)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 4
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class ReferenceSerializer implements FieldSerializer {

  private static final Logger LOGGER = Logger.getLogger(ReferenceSerializer.class.getName());
  private static final int PRIORITY = 15;

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return context.hasAnnotation(Reference.class);
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    final Object value = context.getValue();

    if (value == null) {
      // Store null for null references
      context.putInDocument(null);
    } else {
      // Extract ObjectId from the referenced entity
      final Object objectId = extractObjectId(value);

      if (objectId != null) {
        // Store only the ObjectId, not the entire object
        context.putInDocument(objectId);
      } else {
        // No ObjectId found - log warning and store null
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.log(
              Level.WARNING,
              "Reference field {0} in class {1} points to an entity without @ObjectId. "
                  + "Storing null. Entity class: {2}",
              new Object[] {
                context.getFieldName(),
                context.getEntity().getClass().getName(),
                value.getClass().getName()
              });
        }
        context.putInDocument(null);
      }
    }
  }

  /**
   * Extract the ObjectId from a referenced entity.
   *
   * @param entity the referenced entity
   * @return the ObjectId value, or null if not found
   */
  private Object extractObjectId(final Object entity) {
    // Look for field with @ObjectId annotation
    final Class<?> entityClass = entity.getClass();
    Object result = null;

    for (final Field field : entityClass.getDeclaredFields()) {
      if (field.isAnnotationPresent(ObjectId.class)) {
        try {
          field.setAccessible(true);
          result = field.get(entity);
          break;
        } catch (final IllegalAccessException e) {
          throw new MappingException(
              "Failed to access @ObjectId field in referenced entity: " + entityClass.getName(), e);
        }
      }
    }

    // No @ObjectId field found
    return result;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "ReferenceSerializer{priority=" + PRIORITY + "}";
  }
}
