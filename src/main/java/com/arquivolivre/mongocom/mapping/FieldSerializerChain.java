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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chain of Responsibility for field serialization.
 *
 * <p>This class manages a chain of {@link FieldSerializer} instances and delegates serialization to
 * the first serializer that can handle the field. Serializers are executed in priority order (lower
 * priority number = higher priority).
 *
 * <p><b>Design Pattern:</b> Chain of Responsibility - each serializer in the chain gets a chance to
 * handle the field.
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. The serializers list is immutable after
 * construction.
 *
 * <p><b>Complexity Reduction:</b> This pattern reduces complexity from 23 (original loadDocument
 * method) to ~3 per serializer.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class FieldSerializerChain {

  private static final Logger LOG = Logger.getLogger(FieldSerializerChain.class.getName());

  private final List<FieldSerializer> serializers;

  /**
   * Private constructor - use Builder to create instances.
   *
   * @param serializers list of serializers (will be sorted by priority)
   */
  private FieldSerializerChain(final List<FieldSerializer> serializers) {
    // Sort by priority and make immutable
    final List<FieldSerializer> sorted = new ArrayList<>(serializers);
    sorted.sort(Comparator.comparingInt(FieldSerializer::getPriority));
    this.serializers = List.copyOf(sorted);

    LOG.log(Level.FINE, "Created FieldSerializerChain with {0} serializers", serializers.size());
  }

  /**
   * Serialize field using chain.
   *
   * <p>Iterates through serializers in priority order until one can handle the field. The first
   * serializer that returns true from {@link FieldSerializer#canHandle(FieldSerializationContext)}
   * will serialize the field.
   *
   * <p><b>Complexity:</b> 3 (simple loop with early return)
   *
   * @param context the serialization context (must not be null)
   */
  public void serialize(final FieldSerializationContext context) {
    for (final FieldSerializer serializer : serializers) {
      if (serializer.canHandle(context)) {
        LOG.log(
            Level.FINE,
            "Serializing field ''{0}'' with {1}",
            new Object[] {context.getFieldName(), serializer.getClass().getSimpleName()});

        serializer.serialize(context);
        return;
      }
    }

    throw new MappingException(
        "No serializer found for field: "
            + context.getFieldName()
            + " of type: "
            + context.getFieldType().getName());
  }

  /**
   * Get number of serializers in chain.
   *
   * @return serializer count
   */
  public int size() {
    return serializers.size();
  }

  /**
   * Get list of serializers (unmodifiable).
   *
   * @return list of serializers
   */
  public List<FieldSerializer> getSerializers() {
    return serializers;
  }

  /**
   * Create a new builder for FieldSerializerChain.
   *
   * @return new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for creating custom serializer chains.
   *
   * <p>Provides a fluent API for constructing FieldSerializerChain instances with custom
   * serializers.
   */
  public static final class Builder {
    private final List<FieldSerializer> serializers = new ArrayList<>();

    private Builder() {}

    /**
     * Add serializer to chain.
     *
     * @param serializer the serializer to add (must not be null)
     * @return this builder for method chaining
     * @throws NullPointerException if serializer is null
     */
    public Builder add(final FieldSerializer serializer) {
      if (serializer == null) {
        throw new NullPointerException("Serializer cannot be null");
      }
      serializers.add(serializer);
      return this;
    }

    /**
     * Add multiple serializers to chain.
     *
     * @param serializersToAdd serializers to add (must not be null)
     * @return this builder for method chaining
     * @throws NullPointerException if serializersToAdd is null
     */
    public Builder addAll(final List<FieldSerializer> serializersToAdd) {
      if (serializersToAdd == null) {
        throw new NullPointerException("Serializers list cannot be null");
      }
      serializers.addAll(serializersToAdd);
      return this;
    }

    /**
     * Build immutable chain.
     *
     * @return new FieldSerializerChain instance
     * @throws IllegalStateException if no serializers have been added
     */
    public FieldSerializerChain build() {
      if (serializers.isEmpty()) {
        throw new IllegalStateException("At least one serializer required");
      }
      return new FieldSerializerChain(serializers);
    }
  }
}
