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

import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.mapping.FieldSerializer;
import com.arquivolivre.mongocom.mapping.FieldSerializerChain;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Serializer for nested object fields.
 *
 * <p>This serializer handles complex object fields by recursively serializing all their fields
 * using the full FieldSerializerChain. This ensures proper handling of annotations, references, and
 * other special cases in nested objects.
 *
 * <p><b>Serialization Strategy:</b>
 *
 * <ul>
 *   <li>Recursively serializes all fields of the nested object
 *   <li>Uses full chain for each field (supports @Internal, @ObjectId, @Reference, etc.)
 *   <li>Handles class hierarchy (includes inherited fields)
 *   <li>Skips static fields
 * </ul>
 *
 * <p><b>Priority:</b> 25 (type-based, must run before default serializer but after specific types)
 *
 * <p><b>Thread Safety:</b> Uses lazy-initialized serializer chain with double-checked locking.
 *
 * <p><b>Cyclomatic Complexity:</b> 5
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class NestedObjectSerializer implements FieldSerializer {

  private static final int PRIORITY = 25;
  private volatile FieldSerializerChain serializerChain;
  private final EntityMetadataExtractor metadataExtractor;

  /** Creates a new NestedObjectSerializer with a new metadata extractor. */
  public NestedObjectSerializer() {
    this.metadataExtractor = new EntityMetadataExtractor();
  }

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    // Handle any non-primitive, non-collection object
    final Class<?> type = context.getFieldType();
    return !type.isPrimitive()
        && !type.isArray()
        && !type.isEnum()
        && !List.class.isAssignableFrom(type)
        && !type.getName().startsWith("java.lang.")
        && !type.getName().startsWith("java.util.");
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    final Object nestedObject = context.getValue();
    final Document nestedDocument = serializeNestedObject(nestedObject);
    context.putInDocument(nestedDocument);
  }

  /**
   * Serializes a nested object using the full serializer chain.
   *
   * <p>This method ensures that nested objects are properly serialized with support for all
   * annotations (@Internal, @ObjectId, @Reference, etc.).
   *
   * @param obj the nested object to serialize
   * @return a Document containing the serialized object
   * @throws MappingException if serialization fails
   */
  private Document serializeNestedObject(final Object obj) {
    final Document document = new Document();
    final FieldSerializerChain chain = getSerializerChain();
    final EntityMetadata metadata = metadataExtractor.getMetadata(obj.getClass());

    for (final Field field : getAllFields(obj.getClass())) {
      field.setAccessible(true);
      try {
        final Object value = field.get(obj);
        final FieldSerializationContext fieldContext =
            new FieldSerializationContext(field, value, obj, document, metadata);
        chain.serialize(fieldContext);
      } catch (final IllegalAccessException e) {
        throw new MappingException("Failed to access field: " + field.getName(), e);
      }
    }

    return document;
  }

  /**
   * Gets all fields from the class hierarchy.
   *
   * @param clazz the class to inspect
   * @return list of all fields including inherited ones
   */
  private List<Field> getAllFields(final Class<?> clazz) {
    final List<Field> fields = new ArrayList<>();
    Class<?> current = clazz;

    while (current != null && current != Object.class) {
      for (final Field field : current.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())) {
          fields.add(field);
        }
      }
      current = current.getSuperclass();
    }

    return fields;
  }

  /**
   * Gets the serializer chain, initializing it lazily if needed.
   *
   * <p>Uses double-checked locking for thread-safe lazy initialization.
   *
   * @return the serializer chain
   */
  private FieldSerializerChain getSerializerChain() {
    if (serializerChain == null) {
      synchronized (this) {
        if (serializerChain == null) {
          serializerChain =
              FieldSerializerChain.builder()
                  .add(new NullValueSerializer())
                  .add(new InternalFieldSerializer())
                  .add(new ObjectIdSerializer())
                  .add(new ReferenceSerializer())
                  .add(new ListSerializer())
                  .add(new EnumSerializer())
                  .add(new PrimitiveSerializer())
                  .build();
        }
      }
    }
    return serializerChain;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "NestedObjectSerializer{priority=" + PRIORITY + "}";
  }
}
