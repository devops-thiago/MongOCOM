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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Serializer for List fields.
 *
 * <p>This serializer handles List fields by recursively serializing each element. For nested
 * objects, it uses the full FieldSerializerChain to ensure proper handling of annotations,
 * references, and other special cases.
 *
 * <p><b>Serialization Strategy:</b>
 *
 * <ul>
 *   <li>Primitives/Strings: Direct serialization
 *   <li>Nested Objects: Full chain-based serialization with annotation support
 *   <li>Empty Lists: Serialized as empty arrays
 * </ul>
 *
 * <p><b>Priority:</b> 20 (collection-based, must run before nested object serializer)
 *
 * <p><b>Thread Safety:</b> Uses lazy-initialized serializer chain with double-checked locking.
 *
 * <p><b>Cyclomatic Complexity:</b> 6
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class ListSerializer implements FieldSerializer {

  private static final int PRIORITY = 20;
  private volatile FieldSerializerChain serializerChain;

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return List.class.isAssignableFrom(context.getFieldType());
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    final List<?> list = (List<?>) context.getValue();
    final List<Object> serializedList = new ArrayList<>(list.size());

    for (final Object element : list) {
      if (element == null) {
        serializedList.add(null);
      } else if (isPrimitive(element.getClass())) {
        serializedList.add(element);
      } else {
        serializedList.add(serializeNestedObject(element));
      }
    }

    context.putInDocument(serializedList);
  }

  /**
   * Serializes a nested object using the full serializer chain.
   *
   * <p>This method ensures that nested objects are properly serialized with support for all
   * annotations (@Internal, @ObjectId, @Reference, etc.).
   *
   * <p>Note: For nested objects in lists, we create a minimal EntityMetadata since we don't have
   * the full metadata context. This is acceptable because nested objects are typically simple value
   * objects without complex entity relationships.
   *
   * @param obj the nested object to serialize
   * @return a Document containing the serialized object
   * @throws MappingException if serialization fails
   */
  private Document serializeNestedObject(final Object obj) {
    final Document document = new Document();
    final FieldSerializerChain chain = getSerializerChain();

    for (final Field field : getAllFields(obj.getClass())) {
      field.setAccessible(true);
      try {
        final Object value = field.get(obj);
        // Create a minimal context for nested object serialization
        // We pass null for metadata since nested objects don't need full entity metadata
        final FieldSerializationContext fieldContext =
            new FieldSerializationContext(field, value, obj, document, null);
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
                  .add(new EnumSerializer())
                  .add(new PrimitiveSerializer())
                  .build();
        }
      }
    }
    return serializerChain;
  }

  /**
   * Checks if a class is a primitive or wrapper type.
   *
   * @param clazz the class to check
   * @return true if primitive or wrapper, false otherwise
   */
  private boolean isPrimitive(final Class<?> clazz) {
    return clazz.isPrimitive()
        || clazz == Boolean.class
        || clazz == Byte.class
        || clazz == Short.class
        || clazz == Integer.class
        || clazz == Long.class
        || clazz == Float.class
        || clazz == Double.class
        || clazz == Character.class
        || clazz == String.class;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "ListSerializer{priority=" + PRIORITY + "}";
  }
}
