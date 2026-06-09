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

/**
 * Serializer for enum fields.
 *
 * <p>This serializer converts enum constants to their String name using {@link Enum#name()},
 * ensuring consistency with deserialization.
 *
 * <p><b>Priority:</b> 30 (type-based, must run before default serializer)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class EnumSerializer implements FieldSerializer {

  private static final int PRIORITY = 30;

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return context.getFieldType().isEnum();
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    final Object value = context.getValue();

    if (value instanceof Enum) {
      // Store enum name (not toString which may be overridden)
      final String enumName = ((Enum<?>) value).name();
      context.putInDocument(enumName);
    }
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "EnumSerializer{priority=" + PRIORITY + "}";
  }
}
