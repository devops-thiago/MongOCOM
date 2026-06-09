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

package com.arquivolivre.mongocom.references;

import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles saving of referenced entities.
 *
 * <p>This class manages the persistence of entities that are referenced by other entities through
 * the @Reference annotation. It prevents infinite loops by tracking already-saved entities and
 * provides options for cascade save behavior.
 *
 * <p><b>Design Pattern:</b> Strategy - defines how references are saved
 *
 * <p><b>Thread Safety:</b> This class is NOT thread-safe. Each thread should use its own instance
 * or synchronize access externally. The savedEntities set is mutable and not synchronized.
 *
 * <p><b>Circular Reference Prevention:</b> Uses a Set to track entities being saved in the current
 * operation, preventing infinite loops.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class ReferenceHandler {

  private static final Logger LOGGER = Logger.getLogger(ReferenceHandler.class.getName());

  private final EntityMetadataExtractor metadataExtractor;
  private final Set<Object> savedEntities;
  private final ReferenceSaveStrategy saveStrategy;

  /** Strategy for saving referenced entities. */
  public enum ReferenceSaveStrategy {
    /** Save all referenced entities recursively. */
    CASCADE_ALL,
    /** Save only direct references (one level deep). */
    CASCADE_DIRECT,
    /** Do not save referenced entities (store only IDs). */
    NO_CASCADE
  }

  /** Creates a new reference handler with default strategy (CASCADE_ALL). */
  public ReferenceHandler() {
    this(ReferenceSaveStrategy.CASCADE_ALL);
  }

  /**
   * Creates a new reference handler with specified strategy.
   *
   * @param saveStrategy the save strategy (must not be null)
   * @throws NullPointerException if saveStrategy is null
   */
  public ReferenceHandler(final ReferenceSaveStrategy saveStrategy) {
    this.metadataExtractor = new EntityMetadataExtractor();
    this.savedEntities = new HashSet<>();
    this.saveStrategy = Objects.requireNonNull(saveStrategy, "Save strategy cannot be null");
  }

  /**
   * Process all @Reference fields in an entity.
   *
   * <p>This method finds all fields annotated with @Reference and processes them according to the
   * configured save strategy.
   *
   * @param entity the entity to process (must not be null)
   * @return set of referenced entity IDs that need to be saved
   * @throws MappingException if reference processing fails
   */
  public Set<String> processReferences(final Object entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");

    final Set<String> referenceIds = new HashSet<>();
    final EntityMetadata metadata = metadataExtractor.getMetadata(entity.getClass());

    // Check for circular reference
    if (!savedEntities.contains(entity)) {
      // Mark entity as being processed
      savedEntities.add(entity);

      try {
        // Process each reference field
        for (final Field field : metadata.getReferenceFields()) {
          final String refId = processReferenceField(entity, field);
          if (refId != null) {
            referenceIds.add(refId);
          }
        }
      } finally {
        // Remove from tracking after processing
        savedEntities.remove(entity);
      }
    } else {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(
            Level.FINE, "Circular reference detected for entity: {0}", entity.getClass().getName());
      }
    }

    return referenceIds;
  }

  /**
   * Process a single reference field.
   *
   * @param entity the entity containing the field
   * @param field the reference field
   * @return the ID of the referenced entity, or null if field is null
   * @throws MappingException if field processing fails
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  private String processReferenceField(final Object entity, final Field field) {
    String result = null;
    try {
      field.setAccessible(true);
      final Object referencedEntity = field.get(entity);

      if (referencedEntity != null) {
        // Get ID from referenced entity
        result = extractEntityId(referencedEntity);
      }

    } catch (final IllegalAccessException e) {
      throw new MappingException(
          "Failed to access reference field: " + field.getName() + " in " + entity.getClass(), e);
    }
    return result;
  }

  /**
   * Extract ID from an entity.
   *
   * @param entity the entity
   * @return the entity ID as String
   * @throws MappingException if ID cannot be extracted
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  private String extractEntityId(final Object entity) {
    final EntityMetadata metadata = metadataExtractor.getMetadata(entity.getClass());
    final Field idField = metadata.getIdField();

    if (idField == null) {
      throw new MappingException(
          "Referenced entity " + entity.getClass().getName() + " has no @Id field");
    }

    final String result;
    try {
      idField.setAccessible(true);
      final Object idValue = idField.get(entity);

      if (idValue == null) {
        throw new MappingException(
            "Referenced entity " + entity.getClass().getName() + " has null ID");
      }

      result = idValue.toString();

    } catch (final IllegalAccessException e) {
      throw new MappingException(
          "Failed to access ID field in referenced entity: " + entity.getClass(), e);
    }
    return result;
  }

  /**
   * Check if an entity should be saved based on strategy.
   *
   * @param depth the current depth in the reference chain
   * @return true if entity should be saved
   */
  public boolean shouldSaveReference(final int depth) {
    final boolean result;
    switch (saveStrategy) {
      case CASCADE_ALL:
        result = true;
        break;
      case CASCADE_DIRECT:
        result = depth <= 1;
        break;
      case NO_CASCADE:
        result = false;
        break;
      default:
        result = false;
        break;
    }
    return result;
  }

  /**
   * Clear the saved entities tracking.
   *
   * <p>This should be called after completing a save operation to reset the circular reference
   * detection.
   */
  public void clear() {
    savedEntities.clear();
  }

  /**
   * Get the current save strategy.
   *
   * @return the save strategy
   */
  public ReferenceSaveStrategy getSaveStrategy() {
    return saveStrategy;
  }

  @Override
  public String toString() {
    return "ReferenceHandler{"
        + "strategy="
        + saveStrategy
        + ", trackedEntities="
        + savedEntities.size()
        + '}';
  }
}
