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

package com.arquivolivre.mongocom.metadata;

import com.arquivolivre.mongocom.annotations.GeneratedValue;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe metadata extractor with caching.
 *
 * <p>This class extracts and caches metadata from entity classes using reflection. Metadata
 * extraction is expensive, so results are cached in a ConcurrentHashMap for lock-free reads after
 * first access.
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Uses ConcurrentHashMap.computeIfAbsent which
 * provides atomic check-and-set semantics.
 *
 * <p><b>Performance:</b> First access per entity class performs reflection (expensive). Subsequent
 * accesses are lock-free reads from cache (fast).
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class EntityMetadataExtractor {

  private static final Logger LOG = Logger.getLogger(EntityMetadataExtractor.class.getName());

  private final ConcurrentMap<Class<?>, EntityMetadata> metadataCache;

  /** Creates a new EntityMetadataExtractor with an empty cache. */
  public EntityMetadataExtractor() {
    this.metadataCache = new ConcurrentHashMap<>();
  }

  /**
   * Get metadata for entity class (cached).
   *
   * <p>This method is thread-safe. ConcurrentHashMap.computeIfAbsent provides atomic check-and-set,
   * ensuring metadata is extracted only once per class even under concurrent access.
   *
   * @param entityClass the entity class (must not be null)
   * @return entity metadata (never null)
   * @throws NullPointerException if entityClass is null
   */
  public EntityMetadata getMetadata(final Class<?> entityClass) {
    return metadataCache.computeIfAbsent(entityClass, this::extractMetadata);
  }

  /**
   * Extract metadata from entity class.
   *
   * <p>This method is called once per class, then results are cached. Performs expensive reflection
   * operations to discover annotated fields.
   *
   * @param clazz the entity class
   * @return extracted metadata
   */
  private EntityMetadata extractMetadata(final Class<?> clazz) {
    LOG.log(Level.FINE, "Extracting metadata for: {0}", clazz.getName());

    return EntityMetadata.builder()
        .entityClass(clazz)
        .collectionName(extractCollectionName(clazz))
        .idField(AnnotationIntrospector.getFieldByAnnotation(clazz, Id.class))
        .objectIdField(
            AnnotationIntrospector.getFieldByAnnotation(
                clazz, com.arquivolivre.mongocom.annotations.ObjectId.class))
        .indexedFields(AnnotationIntrospector.getFieldsByAnnotation(clazz, Index.class))
        .referenceFields(AnnotationIntrospector.getFieldsByAnnotation(clazz, Reference.class))
        .internalFields(AnnotationIntrospector.getFieldsByAnnotation(clazz, Internal.class))
        .generatedFields(AnnotationIntrospector.getFieldsByAnnotation(clazz, GeneratedValue.class))
        .build();
  }

  /**
   * Extract collection name from @Document annotation.
   *
   * <p>If the @Document annotation specifies a collection name, that name is used. Otherwise, the
   * simple class name is used as the collection name.
   *
   * @param clazz the entity class
   * @return collection name (never null)
   */
  private String extractCollectionName(final Class<?> clazz) {
    final com.arquivolivre.mongocom.annotations.Document doc =
        clazz.getAnnotation(com.arquivolivre.mongocom.annotations.Document.class);

    final String result;
    if (doc != null && !doc.collection().isEmpty()) {
      result = doc.collection();
    } else {
      result = clazz.getSimpleName();
    }
    return result;
  }

  /**
   * Clear the metadata cache.
   *
   * <p>This method is primarily useful for testing. In production, the cache should not be cleared
   * as it would force re-extraction of metadata.
   */
  public void clearCache() {
    metadataCache.clear();
    LOG.log(Level.FINE, "Metadata cache cleared");
  }

  /**
   * Get the current cache size.
   *
   * <p>Useful for monitoring and debugging.
   *
   * @return number of cached entity classes
   */
  public int getCacheSize() {
    return metadataCache.size();
  }

  /**
   * Check if metadata is cached for a class.
   *
   * @param entityClass the entity class to check
   * @return true if metadata is cached
   */
  public boolean isCached(final Class<?> entityClass) {
    return metadataCache.containsKey(entityClass);
  }

  /**
   * Pre-load metadata for multiple entity classes.
   *
   * <p>This method can be called at application startup to pre-populate the cache, avoiding the
   * cost of lazy extraction during first use.
   *
   * @param entityClasses classes to pre-load
   */
  public void preloadMetadata(final Class<?>... entityClasses) {
    for (final Class<?> entityClass : entityClasses) {
      getMetadata(entityClass);
    }
    LOG.log(Level.INFO, "Pre-loaded metadata for {0} entity classes", entityClasses.length);
  }
}
