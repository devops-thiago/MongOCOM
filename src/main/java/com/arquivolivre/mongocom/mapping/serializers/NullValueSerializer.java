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
 * Serializer for null field values.
 *
 * <p>This serializer handles null values by skipping them (not including in the document). This
 * reduces document size and storage requirements.
 *
 * <p><b>Priority:</b> 0 (highest - must run first to prevent NPE in other serializers)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * <p><b>Cyclomatic Complexity:</b> 1
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class NullValueSerializer implements FieldSerializer {

  private static final int PRIORITY = 0;

  @Override
  public boolean canHandle(final FieldSerializationContext context) {
    return context.isValueNull();
  }

  @Override
  public void serialize(final FieldSerializationContext context) {
    // Skip null values - don't add to document
    // This reduces document size and is MongoDB best practice
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String toString() {
    return "NullValueSerializer{priority=" + PRIORITY + "}";
  }
}
