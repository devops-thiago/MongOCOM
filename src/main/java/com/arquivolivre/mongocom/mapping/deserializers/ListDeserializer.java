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
import com.arquivolivre.mongocom.mapping.FieldDeserializerChain;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;

/**
 * Deserializer for List fields with generic types.
 *
 * <p>This deserializer handles List fields, extracting the generic type parameter and deserializing
 * each element appropriately. It supports:
 *
 * <ul>
 *   <li>Lists of primitives/wrappers
 *   <li>Lists of Strings
 *   <li>Lists of enums
 *   <li>Lists of nested objects (Documents)
 * </ul>
 *
 * <p><b>Priority:</b> 20 (complex type, must run before simple types)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 5
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class ListDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 20;

  // Lazy-initialized deserializer chain for nested objects
  private static volatile FieldDeserializerChain deserializerChain;

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    return List.class.isAssignableFrom(context.getFieldType());
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Object value = context.getValue();

    if (!(value instanceof List)) {
      throw new MappingException(
          "List field "
              + context.getFieldName()
              + " expects List value, but got: "
              + (value != null ? value.getClass().getName() : "null"));
    }

    @SuppressWarnings("unchecked")
    final List<Object> sourceList = (List<Object>) value;

    // Get generic type parameter
    final Class<?> elementType = getListElementType(context);

    // Create new list and deserialize elements
    final List<Object> targetList = new ArrayList<>(sourceList.size());

    for (final Object element : sourceList) {
      final Object deserializedElement = deserializeElement(element, elementType);
      targetList.add(deserializedElement);
    }

    context.setFieldValue(targetList);
  }

  /**
   * Get the generic type parameter of the List field.
   *
   * @param context the deserialization context
   * @return the element type class
   * @throws MappingException if generic type cannot be determined
   */
  private Class<?> getListElementType(final FieldDeserializationContext context) {
    final Type genericType = context.getField().getGenericType();

    if (!(genericType instanceof ParameterizedType)) {
      throw new MappingException(
          "List field "
              + context.getFieldName()
              + " must have generic type parameter (e.g., List<String>)");
    }

    final ParameterizedType paramType = (ParameterizedType) genericType;
    final Type[] typeArgs = paramType.getActualTypeArguments();

    if (typeArgs.length == 0) {
      throw new MappingException(
          "List field " + context.getFieldName() + " has no generic type parameter");
    }

    if (!(typeArgs[0] instanceof Class)) {
      throw new MappingException(
          "List field "
              + context.getFieldName()
              + " has complex generic type that is not supported: "
              + typeArgs[0]);
    }

    return (Class<?>) typeArgs[0];
  }

  /**
   * Deserialize a single list element.
   *
   * @param element the element to deserialize
   * @param elementType the target element type
   * @return the deserialized element
   * @throws Exception if deserialization fails
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object deserializeElement(final Object element, final Class<?> elementType)
      throws Exception {
    final Object result;

    if (element == null) {
      result = null;
    } else if (element instanceof Document) {
      // Handle nested objects (Documents)
      result = deserializeNestedObject((Document) element, elementType);
    } else if (elementType.isEnum() && element instanceof String) {
      // Handle enums
      result = Enum.valueOf((Class<? extends Enum>) elementType, (String) element);
    } else if (elementType.isInstance(element)) {
      // Handle primitives and simple types - direct assignment or conversion
      result = element;
    } else if (Number.class.isAssignableFrom(elementType) && element instanceof Number) {
      // Type conversion for numbers
      result = convertNumber((Number) element, elementType);
    } else {
      // Fallback: toString conversion
      result = element.toString();
    }

    return result;
  }

  /**
   * Deserialize a nested object from a Document using the full deserialization chain.
   *
   * <p>This method creates an instance of the target type and uses the FieldDeserializerChain to
   * properly deserialize all fields, ensuring that annotations, enums, references, and other
   * complex types are handled correctly.
   *
   * @param doc the document containing field values
   * @param targetType the target type to deserialize into
   * @return the deserialized object
   * @throws Exception if deserialization fails
   */
  private Object deserializeNestedObject(final Document doc, final Class<?> targetType)
      throws Exception {
    // Create new instance
    final Object instance = targetType.getDeclaredConstructor().newInstance();

    // Get metadata for the target type
    final EntityMetadataExtractor extractor = new EntityMetadataExtractor();
    final EntityMetadata metadata = extractor.getMetadata(targetType);

    // Get deserializer chain (lazy initialization)
    final FieldDeserializerChain chain = getDeserializerChain();

    // Get all fields including inherited ones
    final List<Field> fields = getAllFields(targetType);

    // Deserialize each field using the chain
    for (final Field field : fields) {
      field.setAccessible(true);

      // Get value from document
      final String fieldName = field.getName();
      final Object value = doc.get(fieldName);

      // Create deserialization context
      final FieldDeserializationContext context =
          new FieldDeserializationContext(field, value, instance, doc, metadata);

      // Use chain to deserialize
      try {
        chain.deserialize(context);
      } catch (final Exception ignored) {
        // Intentionally ignored - some fields might not be in document
        // This maintains backward compatibility with partial documents
      }
    }

    return instance;
  }

  /**
   * Get all fields from class hierarchy.
   *
   * @param clazz the class to inspect
   * @return list of all fields including inherited ones
   */
  private List<Field> getAllFields(final Class<?> clazz) {
    final List<Field> fields = new ArrayList<>();
    Class<?> current = clazz;

    while (current != null && current != Object.class) {
      fields.addAll(Arrays.asList(current.getDeclaredFields()));
      current = current.getSuperclass();
    }

    return fields;
  }

  /**
   * Get or create the deserializer chain (lazy initialization with double-checked locking).
   *
   * @return the deserializer chain
   */
  private static FieldDeserializerChain getDeserializerChain() {
    if (deserializerChain == null) {
      synchronized (ListDeserializer.class) {
        if (deserializerChain == null) {
          // Create chain with all deserializers except ListDeserializer to avoid recursion
          final List<FieldDeserializer> deserializers =
              Arrays.asList(
                  new NullValueDeserializer(),
                  new InternalFieldDeserializer(),
                  new ObjectIdDeserializer(),
                  new ReferenceDeserializer(),
                  new PrimitiveDeserializer(),
                  new EnumDeserializer(),
                  new NestedObjectDeserializer(),
                  new DefaultDeserializer());

          deserializerChain = new FieldDeserializerChain(deserializers);
        }
      }
    }
    return deserializerChain;
  }

  /**
   * Convert number to target type.
   *
   * @param number the number to convert
   * @param targetType the target type
   * @return the converted number
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  private Object convertNumber(final Number number, final Class<?> targetType) {
    final Object result;

    if (Byte.class.equals(targetType) || byte.class.equals(targetType)) {
      result = number.byteValue();
    } else if (Short.class.equals(targetType) || short.class.equals(targetType)) {
      result = number.shortValue();
    } else if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
      result = number.intValue();
    } else if (Long.class.equals(targetType) || long.class.equals(targetType)) {
      result = number.longValue();
    } else if (Float.class.equals(targetType) || float.class.equals(targetType)) {
      result = number.floatValue();
    } else if (Double.class.equals(targetType) || double.class.equals(targetType)) {
      result = number.doubleValue();
    } else {
      result = number;
    }

    return result;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "ListDeserializer{priority=" + PRIORITY + "}";
  }
}
