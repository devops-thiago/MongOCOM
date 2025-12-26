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

import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.mapping.FieldSerializer;
import java.util.HashSet;
import java.util.Set;

/**
 * Serializer for primitive and wrapper types.
 *
 * <p>This serializer handles all Java primitives and their wrapper classes, as well as String.
 * Values are serialized directly without transformation.
 *
 * <p><b>Supported Types:</b>
 *
 * <ul>
 *   <li>Primitives: boolean, byte, short, int, long, float, double, char
 *   <li>Wrappers: Boolean, Byte, Short, Integer, Long, Float, Double, Character
 *   <li>String
 * </ul>
 *
 * <p><b>Priority:</b> 35 (type-based, must run before default serializer)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class PrimitiveSerializer implements FieldSerializer {

  private static final int PRIORITY = 35;
  private static final Set<Class<?>> PRIMITIVE_TYPES = new HashSet<>();

  static {
    // Primitives
    PRIMITIVE_TYPES.add(boolean.class);
    PRIMITIVE_TYPES.add(byte.class);
    PRIMITIVE_TYPES.add(short.class);
    PRIMITIVE_TYPES.add(int.class);
    PRIMITIVE_TYPES.add(long.class);
    PRIMITIVE_TYPES.add(float.class);
    PRIMITIVE_TYPES.add(double.class);
    PRIMITIVE_TYPES.add(char.class);

    // Wrappers
    PRIMITIVE_TYPES.add(Boolean.class);
    PRIMITIVE_TYPES.add(Byte.class);
    PRIMITIVE_TYPES.add(Short.class);
    PRIMITIVE_TYPES.add(Integer.class);
    PRIMITIVE_TYPES.add(Long.class);
    PRIMITIVE_TYPES.add(Float.class);
    PRIMITIVE_TYPES.add(Double.class);
    PRIMITIVE_TYPES.add(Character.class);

    // String
    PRIMITIVE_TYPES.add(String.class);
  }

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return PRIMITIVE_TYPES.contains(context.getFieldType());
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    // Direct serialization - MongoDB handles these types natively
    context.putInDocument(context.getValue());
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "PrimitiveSerializer{priority=" + PRIORITY + "}";
  }
}
