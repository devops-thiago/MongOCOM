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

import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import com.arquivolivre.mongocom.mapping.FieldDeserializer;
import com.arquivolivre.mongocom.references.ReferenceResolver;
import com.arquivolivre.mongocom.repository.RepositoryFactory;
import com.mongodb.MongoException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deserializer for fields marked with @Reference annotation.
 *
 * <p>This deserializer handles reference resolution during deserialization. It can operate in two
 * modes:
 *
 * <ul>
 *   <li><b>With ReferenceResolver:</b> Fully resolves references by loading referenced entities
 *       from the database
 *   <li><b>Without ReferenceResolver:</b> Stores the ObjectId as a String (placeholder mode)
 * </ul>
 *
 * <p><b>Reference Resolution Features:</b>
 *
 * <ul>
 *   <li>Eager or lazy loading strategies
 *   <li>Circular reference prevention
 *   <li>Entity caching for performance
 *   <li>Configurable resolution depth
 * </ul>
 *
 * <p><b>Priority:</b> 15 (annotation-based, after @ObjectId)
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe. The ReferenceResolver should be
 * used per-thread or synchronized externally.
 *
 * <p><b>Cyclomatic Complexity:</b> 4
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class ReferenceDeserializer implements FieldDeserializer {

  private static final Logger LOGGER = Logger.getLogger(ReferenceDeserializer.class.getName());
  private static final int PRIORITY = 15;

  private final ReferenceResolver referenceResolver;

  /**
   * Creates a reference deserializer without reference resolution (placeholder mode).
   *
   * <p>In this mode, reference fields will store the ObjectId as a String instead of resolving to
   * the actual entity.
   */
  public ReferenceDeserializer() {
    this.referenceResolver = null;
  }

  /**
   * Creates a reference deserializer with full reference resolution.
   *
   * @param repositoryFactory the repository factory for loading referenced entities (must not be
   *     null)
   * @throws NullPointerException if repositoryFactory is null
   */
  public ReferenceDeserializer(final RepositoryFactory repositoryFactory) {
    this.referenceResolver = new ReferenceResolver(repositoryFactory);
  }

  /**
   * Creates a reference deserializer with a custom reference resolver.
   *
   * @param referenceResolver the reference resolver (may be null for placeholder mode)
   */
  public ReferenceDeserializer(final ReferenceResolver referenceResolver) {
    this.referenceResolver = referenceResolver;
  }

  @Override
  public boolean canHandle(final FieldDeserializationContext context) {
    return context.hasAnnotation(Reference.class);
  }

  @Override
  public void deserialize(final FieldDeserializationContext context) throws Exception {
    final Object value = context.getValue();

    if (value == null) {
      return;
    }

    // If we have a reference resolver, use it to load the referenced entity
    if (referenceResolver != null) {
      resolveReference(context, value);
    } else {
      // Placeholder mode: just store the ObjectId as String
      storePlaceholder(context, value);
    }
  }

  /**
   * Resolve the reference by loading the referenced entity.
   *
   * @param context the deserialization context
   * @param value the reference value (ObjectId)
   * @throws IllegalAccessException if field cannot be accessed
   */
  @SuppressWarnings("unchecked")
  private void resolveReference(final FieldDeserializationContext context, final Object value)
      throws IllegalAccessException {
    try {
      // Get the reference ID as String
      final String referenceId = value.toString();

      // Get the field type (the referenced entity class)
      final Class<?> referenceType = context.getFieldType();

      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(
            Level.FINE,
            "Resolving reference for field {0}: type={1}, id={2}",
            new Object[] {context.getFieldName(), referenceType.getName(), referenceId});
      }

      // Use ReferenceResolver to load the referenced entity
      final Object referencedEntity =
          referenceResolver.resolveReference(referenceId, referenceType);

      if (referencedEntity != null) {
        context.setFieldValue(referencedEntity);
      } else {
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.log(
              Level.WARNING,
              "Referenced entity not found: type={0}, id={1}",
              new Object[] {referenceType.getName(), referenceId});
        }
      }

    } catch (final IllegalAccessException e) {
      // Re-throw IllegalAccessException
      throw e;
    } catch (final MappingException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(
            Level.WARNING,
            "Mapping error resolving reference for field " + context.getFieldName(),
            e);
      }
      // Fall back to null
      context.setFieldValue(null);
    } catch (final MongoException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(
            Level.WARNING,
            "Database error resolving reference for field " + context.getFieldName(),
            e);
      }
      // Fall back to null
      context.setFieldValue(null);
    } catch (final IllegalArgumentException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(Level.WARNING, "Invalid reference ID for field " + context.getFieldName(), e);
      }
      // Fall back to null
      context.setFieldValue(null);
    }
  }

  /**
   * Store the ObjectId as a String placeholder.
   *
   * @param context the deserialization context
   * @param value the reference value (ObjectId)
   * @throws IllegalAccessException if field cannot be accessed
   */
  private void storePlaceholder(final FieldDeserializationContext context, final Object value)
      throws IllegalAccessException {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(
          Level.FINE,
          "Storing reference placeholder for field {0} in class {1}",
          new Object[] {context.getFieldName(), context.getTarget().getClass().getName()});
    }

    // Convert ObjectId to String and set
    context.setFieldValue(value.toString());
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  /**
   * Check if this deserializer has reference resolution enabled.
   *
   * @return true if reference resolution is enabled
   */
  public boolean hasReferenceResolution() {
    return referenceResolver != null;
  }

  /**
   * Get the reference resolver (may be null).
   *
   * @return the reference resolver, or null if in placeholder mode
   */
  public ReferenceResolver getReferenceResolver() {
    return referenceResolver;
  }

  @Override
  public String toString() {
    return "ReferenceDeserializer{"
        + "priority="
        + PRIORITY
        + ", mode="
        + (referenceResolver != null ? "FULL_RESOLUTION" : "PLACEHOLDER")
        + '}';
  }
}
