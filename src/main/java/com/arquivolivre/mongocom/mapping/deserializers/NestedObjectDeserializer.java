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
import java.lang.reflect.Field;
import org.bson.Document;

/**
 * Deserializer for nested object fields.
 *
 * <p>This deserializer handles fields that contain nested objects (stored as Documents in MongoDB).
 * It recursively deserializes the nested object by creating a new instance and populating its
 * fields.
 *
 * <p><b>Priority:</b> 25 (complex type, after lists)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 3
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class NestedObjectDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 25;

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    // Handle if value is a Document (nested object)
    return context.getValue() instanceof Document;
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Document doc = (Document) context.getValue();
    final Class<?> fieldType = context.getFieldType();

    // Create new instance of the nested object
    final Object nestedInstance = createInstance(fieldType);

    // Recursively deserialize nested object
    deserializeNestedObject(doc, nestedInstance, fieldType);

    // Set the nested object on the field
    context.setFieldValue(nestedInstance);
  }

  /**
   * Create a new instance of the target type.
   *
   * @param type the type to instantiate
   * @return the new instance
   * @throws MappingException if instantiation fails
   */
  private Object createInstance(final Class<?> type) {
    try {
      return type.getDeclaredConstructor().newInstance();
    } catch (final ReflectiveOperationException e) {
      throw new MappingException(
          "Failed to create instance of nested object type: "
              + type.getName()
              + ". Ensure the class has a no-arg constructor.",
          e);
    } catch (final SecurityException e) {
      throw new MappingException(
          "Security restriction prevents instantiation of: " + type.getName(), e);
    }
  }

  /**
   * Deserialize nested object by populating its fields from the document.
   *
   * <p>This is a simplified implementation for Phase 5. In future phases, this will be enhanced to
   * use the full deserialization chain for proper handling of all field types.
   *
   * @param doc the source document
   * @param target the target object to populate
   * @param targetType the target type
   * @throws Exception if deserialization fails
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  private void deserializeNestedObject(
      final Document doc, final Object target, final Class<?> targetType) throws Exception {

    // Get all fields from the target type
    final Field[] fields = targetType.getDeclaredFields();

    for (final Field field : fields) {
      final String fieldName = field.getName();

      // Skip if document doesn't contain this field
      if (!doc.containsKey(fieldName)) {
        continue;
      }

      final Object value = doc.get(fieldName);

      try {
        // Make field accessible
        field.setAccessible(true);

        // Set value with basic type handling
        setFieldValue(field, target, value);
      } catch (final IllegalAccessException e) {
        // Log warning but continue with other fields
        System.err.println(
            "Warning: Cannot access field "
                + fieldName
                + " on nested object "
                + targetType.getName()
                + ": "
                + e.getMessage());
      } catch (final IllegalArgumentException e) {
        // Log warning but continue with other fields
        System.err.println(
            "Warning: Invalid value for field "
                + fieldName
                + " on nested object "
                + targetType.getName()
                + ": "
                + e.getMessage());
      }
    }
  }

  /**
   * Set field value with basic type handling.
   *
   * @param field the field to set
   * @param target the target object
   * @param value the value to set
   * @throws IllegalAccessException if field cannot be accessed
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setFieldValue(final Field field, final Object target, final Object value)
      throws IllegalAccessException {

    if (value == null) {
      field.set(target, null);
    } else {
      final Class<?> fieldType = field.getType();

      if (fieldType.isInstance(value)) {
        // Direct assignment if types match
        field.set(target, value);
      } else if (fieldType.isEnum() && value instanceof String) {
        // Handle enum conversion
        final Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) fieldType, (String) value);
        field.set(target, enumValue);
      } else if (Number.class.isAssignableFrom(fieldType) && value instanceof Number) {
        // Handle number conversion
        final Object convertedNumber = convertNumber((Number) value, fieldType);
        field.set(target, convertedNumber);
      } else {
        // Fallback: direct assignment (may throw ClassCastException)
        field.set(target, value);
      }
    }
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
    return "NestedObjectDeserializer{priority=" + PRIORITY + "}";
  }
}
