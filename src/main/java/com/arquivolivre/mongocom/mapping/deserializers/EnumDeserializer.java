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

/**
 * Deserializer for enum fields.
 *
 * <p>This deserializer converts String values from documents to enum constants using {@link
 * Enum#valueOf(Class, String)}.
 *
 * <p><b>Priority:</b> 30 (simple type, after annotations and complex types)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 2
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class EnumDeserializer implements FieldDeserializer {

  private static final int PRIORITY = 30;

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    return context.isFieldEnum();
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Object value = context.getValue();

    if (!(value instanceof String)) {
      throw new MappingException(
          "Enum field "
              + context.getFieldName()
              + " expects String value, but got: "
              + (value != null ? value.getClass().getName() : "null"));
    }

    final String enumName = (String) value;
    final Class<? extends Enum> enumClass = (Class<? extends Enum>) context.getFieldType();

    try {
      final Enum<?> enumValue = Enum.valueOf(enumClass, enumName);
      context.setFieldValue(enumValue);
    } catch (final IllegalArgumentException e) {
      throw new MappingException(
          "Invalid enum value '"
              + enumName
              + "' for enum type "
              + enumClass.getName()
              + " in field "
              + context.getFieldName(),
          e);
    }
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "EnumDeserializer{priority=" + PRIORITY + "}";
  }
}
