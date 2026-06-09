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
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializer for primitive and wrapper types.
 *
 * <p>This deserializer handles conversion between MongoDB types and Java primitives/wrappers,
 * including type coercion (e.g., Integer to Long, Float to Double).
 *
 * <p>Supported types:
 *
 * <ul>
 *   <li>boolean/Boolean
 *   <li>byte/Byte
 *   <li>short/Short
 *   <li>int/Integer
 *   <li>long/Long
 *   <li>float/Float
 *   <li>double/Double
 *   <li>char/Character
 * </ul>
 *
 * <p><b>Priority:</b> 35 (simple type, after enums)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe. The type map is immutable.
 *
 * <p><b>Cyclomatic Complexity:</b> 4
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class PrimitiveDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 35;

  /** Expected length for single character strings. */
  private static final int SINGLE_CHAR_LENGTH = 1;

  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = createPrimitiveMap();

  private static Map<Class<?>, Class<?>> createPrimitiveMap() {
    final Map<Class<?>, Class<?>> map = new HashMap<>();
    map.put(boolean.class, Boolean.class);
    map.put(byte.class, Byte.class);
    map.put(short.class, Short.class);
    map.put(int.class, Integer.class);
    map.put(long.class, Long.class);
    map.put(float.class, Float.class);
    map.put(double.class, Double.class);
    map.put(char.class, Character.class);
    return Map.copyOf(map); // Immutable
  }

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    final Class<?> fieldType = context.getFieldType();
    return fieldType.isPrimitive() || PRIMITIVE_TO_WRAPPER.containsValue(fieldType);
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Object value = context.getValue();
    final Class<?> fieldType = context.getFieldType();

    // Get wrapper class if primitive
    final Class<?> targetType =
        fieldType.isPrimitive() ? PRIMITIVE_TO_WRAPPER.get(fieldType) : fieldType;

    try {
      final Object convertedValue = convertValue(value, targetType);
      context.setFieldValue(convertedValue);
    } catch (final IllegalAccessException e) {
      throw new MappingException("Cannot access field: " + context.getFieldName(), e);
    } catch (final IllegalArgumentException | ClassCastException e) {
      throw new MappingException(
          "Failed to convert value '"
              + value
              + "' to type "
              + targetType.getName()
              + " for field "
              + context.getFieldName(),
          e);
    }
  }

  /**
   * Convert value to target type with type coercion.
   *
   * @param value the value to convert
   * @param targetType the target type
   * @return the converted value
   */
  private Object convertValue(final Object value, final Class<?> targetType) {
    Object result;

    if (value == null) {
      return null;
    } else if (targetType.isInstance(value)) {
      // Direct assignment if types match
      result = value;
    } else if (Number.class.isAssignableFrom(targetType) && value instanceof Number) {
      // Type coercion for numbers
      result = convertNumber((Number) value, targetType);
    } else if (Boolean.class.equals(targetType)) {
      // Boolean conversion
      result = Boolean.valueOf(value.toString());
    } else if (Character.class.equals(targetType)) {
      // Character conversion
      final String str = value.toString();
      if (str.length() != SINGLE_CHAR_LENGTH) {
        throw new IllegalArgumentException("Cannot convert '" + str + "' to Character");
      }
      result = str.charAt(0);
    } else {
      // Fallback: try to parse from string
      result = parseFromString(value.toString(), targetType);
    }

    return result;
  }

  /**
   * Convert number to target numeric type.
   *
   * @param number the number to convert
   * @param targetType the target type
   * @return the converted number
   */
  private Object convertNumber(final Number number, final Class<?> targetType) {
    final Object result;

    if (Byte.class.equals(targetType)) {
      result = number.byteValue();
    } else if (Short.class.equals(targetType)) {
      result = number.shortValue();
    } else if (Integer.class.equals(targetType)) {
      result = number.intValue();
    } else if (Long.class.equals(targetType)) {
      result = number.longValue();
    } else if (Float.class.equals(targetType)) {
      result = number.floatValue();
    } else if (Double.class.equals(targetType)) {
      result = number.doubleValue();
    } else {
      result = number;
    }

    return result;
  }

  /**
   * Parse value from string representation.
   *
   * @param str the string to parse
   * @param targetType the target type
   * @return the parsed value
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  private Object parseFromString(final String str, final Class<?> targetType) {
    final Object result;

    if (Byte.class.equals(targetType)) {
      result = Byte.valueOf(str);
    } else if (Short.class.equals(targetType)) {
      result = Short.valueOf(str);
    } else if (Integer.class.equals(targetType)) {
      result = Integer.valueOf(str);
    } else if (Long.class.equals(targetType)) {
      result = Long.valueOf(str);
    } else if (Float.class.equals(targetType)) {
      result = Float.valueOf(str);
    } else if (Double.class.equals(targetType)) {
      result = Double.valueOf(str);
    } else if (Boolean.class.equals(targetType)) {
      result = Boolean.valueOf(str);
    } else {
      throw new IllegalArgumentException("Cannot parse '" + str + "' to " + targetType.getName());
    }

    return result;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "PrimitiveDeserializer{priority=" + PRIORITY + "}";
  }
}
