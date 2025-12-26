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
import com.arquivolivre.mongocom.repository.EntityRepository;
import com.arquivolivre.mongocom.repository.RepositoryFactory;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves referenced entities during deserialization.
 *
 * <p>This class handles loading of entities that are referenced by other entities through the
 * Reference annotation. It provides caching to avoid redundant database queries and prevents
 * infinite loops when resolving circular references.
 *
 * <p><b>Design Pattern:</b> Strategy - defines how references are resolved
 *
 * <p><b>Design Pattern:</b> Cache - caches resolved entities to avoid redundant queries
 *
 * <p><b>Thread Safety:</b> This class is NOT thread-safe. Each thread should use its own instance
 * or synchronize access externally. The cache and tracking sets are mutable and not synchronized.
 *
 * <p><b>Performance:</b> Caching significantly reduces database queries for frequently referenced
 * entities.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class ReferenceResolver {

  private static final Logger LOGGER = Logger.getLogger(ReferenceResolver.class.getName());

  private final RepositoryFactory repositoryFactory;
  private final EntityMetadataExtractor metadataExtractor;
  private final Map<String, Object> entityCache;
  private final Set<String> resolvingIds;
  private final ReferenceLoadStrategy loadStrategy;

  /** Strategy for loading referenced entities. */
  public enum ReferenceLoadStrategy {
    /** Load all referenced entities eagerly. */
    EAGER,
    /** Load only direct references (one level deep). */
    EAGER_DIRECT,
    /** Do not load referenced entities (lazy loading placeholder). */
    LAZY
  }

  /**
   * Creates a new reference resolver with default strategy (EAGER).
   *
   * @param repositoryFactory the repository factory (must not be null)
   * @throws NullPointerException if repositoryFactory is null
   */
  public ReferenceResolver(final RepositoryFactory repositoryFactory) {
    this(repositoryFactory, ReferenceLoadStrategy.EAGER);
  }

  /**
   * Creates a new reference resolver with specified strategy.
   *
   * @param repositoryFactory the repository factory (must not be null)
   * @param loadStrategy the load strategy (must not be null)
   * @throws NullPointerException if any parameter is null
   */
  public ReferenceResolver(
      final RepositoryFactory repositoryFactory, final ReferenceLoadStrategy loadStrategy) {
    this.repositoryFactory =
        Objects.requireNonNull(repositoryFactory, "Repository factory cannot be null");
    this.loadStrategy = Objects.requireNonNull(loadStrategy, "Load strategy cannot be null");
    this.metadataExtractor = new EntityMetadataExtractor();
    this.entityCache = new HashMap<>();
    this.resolvingIds = new HashSet<>();
  }

  /**
   * Resolve all @Reference fields in an entity.
   *
   * <p>This method finds all fields annotated with @Reference and loads the referenced entities
   * from the database according to the configured load strategy.
   *
   * @param entity the entity to process (must not be null)
   * @throws MappingException if reference resolution fails
   */
  public void resolveReferences(final Object entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");

    final EntityMetadata metadata = metadataExtractor.getMetadata(entity.getClass());

    // Process each reference field
    for (final Field field : metadata.getReferenceFields()) {
      resolveReferenceField(entity, field, 0);
    }
  }

  /**
   * Resolve a single reference field.
   *
   * @param entity the entity containing the field
   * @param field the reference field
   * @param depth the current depth in the reference chain
   * @throws MappingException if field resolution fails
   */
  @SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "unchecked"})
  private void resolveReferenceField(final Object entity, final Field field, final int depth) {
    try {
      field.setAccessible(true);
      final Object fieldValue = field.get(entity);

      if (fieldValue != null) {
        // Check if we should load this reference based on strategy
        if (shouldLoadReference(depth)) {
          // Get the ID (currently stored as String from Phase 5 placeholder)
          final String referenceId = fieldValue.toString();

          // Check for circular reference
          if (!resolvingIds.contains(referenceId)) {
            // Get the referenced entity type
            final Class<?> referenceType = field.getType();

            // Resolve the reference
            final Object referencedEntity = resolveReference(referenceId, referenceType, depth);

            // Set the resolved entity
            if (referencedEntity != null) {
              field.set(entity, referencedEntity);
            }
          } else {
            if (LOGGER.isLoggable(Level.WARNING)) {
              LOGGER.log(
                  Level.WARNING,
                  "Circular reference detected for ID: {0} in field: {1}",
                  new Object[] {referenceId, field.getName()});
            }
          }
        } else {
          if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(
                Level.FINE,
                "Skipping reference resolution for field {0} at depth {1}",
                new Object[] {field.getName(), depth});
          }
        }
      }

    } catch (final IllegalAccessException e) {
      throw new MappingException(
          "Failed to access reference field: " + field.getName() + " in " + entity.getClass(), e);
    }
  }

  /**
   * Resolve a reference by ID and type.
   *
   * @param id the entity ID
   * @param type the entity type
   * @param depth the current depth
   * @return the resolved entity, or null if not found
   * @throws MappingException if resolution fails
   */
  @SuppressWarnings("unchecked")
  private <T> T resolveReference(final String id, final Class<T> type, final int depth) {
    final T result;

    // Check cache first
    final String cacheKey = type.getName() + ":" + id;
    if (entityCache.containsKey(cacheKey)) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(Level.FINE, "Cache hit for reference: {0}", cacheKey);
      }
      result = (T) entityCache.get(cacheKey);
    } else {
      // Mark as resolving to prevent circular references
      resolvingIds.add(id);

      try {
        // Get repository for the reference type
        final EntityRepository<T, String> repository = repositoryFactory.getRepository(type);

        // Load the entity
        final Optional<T> optional = repository.findById(id);

        if (optional.isPresent()) {
          final T entity = optional.get();

          // Cache the entity
          entityCache.put(cacheKey, entity);

          // Recursively resolve references in the loaded entity
          if (shouldLoadReference(depth + 1)) {
            resolveReferences(entity);
          }

          result = entity;
        } else {
          if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(
                Level.WARNING,
                "Referenced entity not found: type={0}, id={1}",
                new Object[] {type.getName(), id});
          }
          result = null;
        }

      } finally {
        // Remove from resolving set
        resolvingIds.remove(id);
      }
    }

    return result;
  }

  /**
   * Resolve a reference by ID and type (public API for deserializers).
   *
   * @param <T> the entity type
   * @param referenceId the entity ID
   * @param type the entity type
   * @return the resolved entity, or null if not found
   * @throws MappingException if resolution fails
   */
  public <T> T resolveReference(final String referenceId, final Class<T> type) {
    return resolveReference(referenceId, type, 0);
  }

  /**
   * Check if a reference should be loaded based on strategy and depth.
   *
   * @param depth the current depth in the reference chain
   * @return true if reference should be loaded
   */
  private boolean shouldLoadReference(final int depth) {
    final boolean result;

    switch (loadStrategy) {
      case EAGER:
        result = true;
        break;
      case EAGER_DIRECT:
        result = depth <= 1;
        break;
      case LAZY:
        result = false;
        break;
      default:
        result = false;
        break;
    }

    return result;
  }

  /**
   * Clear the entity cache and resolving tracking.
   *
   * <p>This should be called after completing a load operation to free memory and reset circular
   * reference detection.
   */
  public void clear() {
    entityCache.clear();
    resolvingIds.clear();
  }

  /**
   * Get the current cache size.
   *
   * @return number of cached entities
   */
  public int getCacheSize() {
    return entityCache.size();
  }

  /**
   * Get the current load strategy.
   *
   * @return the load strategy
   */
  public ReferenceLoadStrategy getLoadStrategy() {
    return loadStrategy;
  }

  @Override
  public String toString() {
    return "ReferenceResolver{"
        + "strategy="
        + loadStrategy
        + ", cachedEntities="
        + entityCache.size()
        + ", resolving="
        + resolvingIds.size()
        + '}';
  }
}
