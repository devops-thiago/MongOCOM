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

package com.arquivolivre.mongocom.mapping;

import com.arquivolivre.mongocom.exception.MappingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chain of Responsibility for field deserialization.
 *
 * <p>This class manages a chain of {@link FieldDeserializer} instances, executing them in priority
 * order until one handles the field.
 *
 * <p><b>Design Pattern:</b> Chain of Responsibility - passes deserialization request through a
 * chain of handlers.
 *
 * <p><b>Complexity Reduction:</b> Original loadObject() had cyclomatic complexity of 20+. This
 * chain reduces it to 3 by delegating to specialized deserializers.
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. The deserializer list is immutable after
 * construction.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class FieldDeserializerChain {

  private static final Logger LOGGER = Logger.getLogger(FieldDeserializerChain.class.getName());

  private final List<FieldDeserializer> deserializers;

  /**
   * Creates a new deserializer chain with the given deserializers.
   *
   * <p>Deserializers are automatically sorted by priority (lower = higher priority).
   *
   * @param deserializerList the list of deserializers (must not be null or empty)
   * @throws NullPointerException if deserializerList is null
   * @throws IllegalArgumentException if deserializerList is empty
   */
  public FieldDeserializerChain(final List<FieldDeserializer> deserializerList) {
    Objects.requireNonNull(deserializerList, "Deserializer list cannot be null");
    if (deserializerList.isEmpty()) {
      throw new IllegalArgumentException("Deserializer list cannot be empty");
    }

    // Create immutable sorted copy
    this.deserializers = new ArrayList<>(deserializerList);
    this.deserializers.sort(Comparator.comparingInt(FieldDeserializer::getPriority));

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(
          Level.FINE,
          "Initialized FieldDeserializerChain with {0} deserializers",
          deserializers.size());
    }
  }

  /**
   * Deserialize a field value and set it on the target object.
   *
   * <p>This method iterates through the chain of deserializers in priority order. The first
   * deserializer that can handle the field will process it.
   *
   * <p><b>Cyclomatic Complexity:</b> 3 (down from 20+ in original loadObject method)
   *
   * @param context the deserialization context (must not be null)
   * @throws MappingException if no deserializer can handle the field or deserialization fails
   */
  public void deserialize(final FieldDeserializationContext context) {
    Objects.requireNonNull(context, "Context cannot be null");

    for (final FieldDeserializer deserializer : deserializers) {
      if (deserializer.canHandle(context)) {
        try {
          deserializer.deserialize(context);
          return; // Successfully handled
        } catch (final MappingException e) {
          // Re-throw MappingException as-is
          throw e;
        } catch (final IllegalAccessException | IllegalArgumentException e) {
          throw new MappingException("Cannot set field value: " + context.getFieldName(), e);
        } catch (final ReflectiveOperationException e) {
          throw new MappingException(
              "Reflection error while deserializing field: " + context.getFieldName(), e);
        } catch (final Exception e) {
          // Catch any other unexpected exceptions and wrap them
          throw new MappingException(
              "Unexpected error while deserializing field: " + context.getFieldName(), e);
        }
      }
    }

    // No deserializer could handle this field
    throw new MappingException(
        "No deserializer found for field: "
            + context.getFieldName()
            + " of type "
            + context.getFieldType().getName());
  }

  /**
   * Get the number of deserializers in the chain.
   *
   * @return the number of deserializers
   */
  public int size() {
    return deserializers.size();
  }

  /**
   * Get an unmodifiable view of the deserializers.
   *
   * @return the list of deserializers (never null)
   */
  public List<FieldDeserializer> getDeserializers() {
    return List.copyOf(deserializers);
  }

  @Override
  public String toString() {
    return "FieldDeserializerChain{" + "deserializers=" + deserializers.size() + '}';
  }
}
