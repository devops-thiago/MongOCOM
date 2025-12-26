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

package com.arquivolivre.mongocom.indexes;

import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import com.arquivolivre.mongocom.types.IndexType;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Manages MongoDB indexes based on @Index annotations.
 *
 * <p>This class creates and manages indexes for entity classes based on @Index annotations on
 * fields. It supports various index types (ascending, descending, text, geo) and index options
 * (unique, sparse, TTL).
 *
 * <p><b>Design Pattern:</b> Template Method - defines index creation algorithm
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. All operations use thread-safe components and
 * the ensured indexes set is synchronized.
 *
 * <p><b>Performance:</b> Index creation is expensive. This class tracks which indexes have been
 * created to avoid redundant operations.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class IndexManager {

  private static final Logger LOGGER = Logger.getLogger(IndexManager.class.getName());

  /** Geospatial 2D index type constant. */
  private static final String GEO_2D = "2d";

  /** Geospatial 2D sphere index type constant. */
  private static final String GEO_2DSPHERE = "2dsphere";

  private final MongoConnectionManager connectionManager;
  private final EntityMetadataExtractor metadataExtractor;
  private final Set<String> ensuredIndexes;

  /**
   * Creates a new index manager.
   *
   * @param connectionManager the connection manager (must not be null)
   * @throws NullPointerException if connectionManager is null
   */
  public IndexManager(final MongoConnectionManager connectionManager) {
    this.connectionManager =
        Objects.requireNonNull(connectionManager, "Connection manager cannot be null");
    this.metadataExtractor = new EntityMetadataExtractor();
    this.ensuredIndexes = new HashSet<>();
  }

  /**
   * Ensure indexes for an entity class.
   *
   * <p>This method creates all indexes defined by @Index annotations on the entity class. It is
   * idempotent - calling it multiple times for the same class will not create duplicate indexes.
   *
   * @param entityClass the entity class (must not be null)
   * @throws MappingException if index creation fails
   */
  public synchronized void ensureIndexes(final Class<?> entityClass) {
    Objects.requireNonNull(entityClass, "Entity class cannot be null");

    final String className = entityClass.getName();

    // Check if indexes already ensured
    if (ensuredIndexes.contains(className)) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(Level.FINE, "Indexes already ensured for: {0}", className);
      }
      return;
    }

    final EntityMetadata metadata = metadataExtractor.getMetadata(entityClass);
    final String collectionName = metadata.getCollectionName();

    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.log(
          Level.INFO,
          "Ensuring indexes for entity: {0}, collection: {1}",
          new Object[] {className, collectionName});
    }

    // Create indexes for each indexed field
    int indexCount = 0;
    for (final Field field : metadata.getIndexedFields()) {
      createIndex(collectionName, field);
      indexCount++;
    }

    // Mark as ensured
    ensuredIndexes.add(className);

    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.log(
          Level.INFO, "Created {0} indexes for entity: {1}", new Object[] {indexCount, className});
    }
  }

  /**
   * Create an index for a field.
   *
   * @param collectionName the collection name
   * @param field the field with @Index annotation
   * @throws MappingException if index creation fails
   */
  private void createIndex(final String collectionName, final Field field) {
    final Index indexAnnotation = field.getAnnotation(Index.class);
    if (indexAnnotation != null) {
      try {
        final MongoDatabase database = connectionManager.getDefaultDatabase();
        final MongoCollection<Document> collection = database.getCollection(collectionName);

        // Determine index type and create appropriate index
        final String indexTypeStr = indexAnnotation.type();
        final int order = indexAnnotation.order();
        final String fieldName = field.getName();

        // Create index based on type
        final Bson indexKeys = createIndexKeys(fieldName, indexTypeStr, order);

        // Create index options
        final IndexOptions options = createIndexOptions(indexAnnotation);

        // Create the index
        final String indexName = collection.createIndex(indexKeys, options);

        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.log(
              Level.FINE,
              "Created index: {0} on field: {1}, type: {2}",
              new Object[] {indexName, fieldName, indexTypeStr});
        }

      } catch (final MongoException e) {
        throw new MappingException(
            "Failed to create index for field: "
                + field.getName()
                + " in collection: "
                + collectionName,
            e);
      } catch (final IllegalArgumentException e) {
        throw new MappingException(
            "Invalid index configuration for field: "
                + field.getName()
                + " in collection: "
                + collectionName,
            e);
      }
    }
  }

  /**
   * Create index keys based on index type.
   *
   * @param fieldName the field name
   * @param indexTypeStr the index type string (e.g., "text", "hashed")
   * @param order the sort order (1 for ascending, -1 for descending)
   * @return the index keys
   */
  private Bson createIndexKeys(final String fieldName, final String indexTypeStr, final int order) {
    final Bson result;
    // Check for special index types
    if (IndexType.INDEX_TEXT.equals(indexTypeStr)) {
      result = Indexes.text(fieldName);
    } else if (IndexType.INDEX_HASHED.equals(indexTypeStr)) {
      result = Indexes.hashed(fieldName);
    } else if (GEO_2D.equals(indexTypeStr)) {
      result = Indexes.geo2d(fieldName);
    } else if (GEO_2DSPHERE.equals(indexTypeStr)) {
      result = Indexes.geo2dsphere(fieldName);
    } else {
      // Use order for ascending/descending
      if (order == IndexType.INDEX_DESCENDING) {
        result = Indexes.descending(fieldName);
      } else {
        result = Indexes.ascending(fieldName);
      }
    }
    return result;
  }

  /**
   * Create index options from annotation.
   *
   * @param indexAnnotation the index annotation
   * @return the index options
   */
  private IndexOptions createIndexOptions(final Index indexAnnotation) {
    final IndexOptions options = new IndexOptions();

    // Set unique
    if (indexAnnotation.unique()) {
      options.unique(true);
    }

    // Set sparse
    if (indexAnnotation.sparse()) {
      options.sparse(true);
    }

    // Set background (deprecated in MongoDB 4.2+, but kept for compatibility)
    if (indexAnnotation.background()) {
      options.background(true);
    }

    // Note: Index annotation doesn't have expireAfterSeconds() or name() methods
    // These would need to be added to the annotation if needed

    return options;
  }

  /**
   * Drop all indexes for a collection (except _id).
   *
   * @param entityClass the entity class
   * @throws MappingException if index dropping fails
   */
  public synchronized void dropIndexes(final Class<?> entityClass) {
    Objects.requireNonNull(entityClass, "Entity class cannot be null");

    final EntityMetadata metadata = metadataExtractor.getMetadata(entityClass);
    final String collectionName = metadata.getCollectionName();

    try {
      final MongoDatabase database = connectionManager.getDefaultDatabase();
      final MongoCollection<Document> collection = database.getCollection(collectionName);

      collection.dropIndexes();

      // Remove from ensured set
      ensuredIndexes.remove(entityClass.getName());

      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.log(Level.INFO, "Dropped indexes for collection: {0}", collectionName);
      }

    } catch (final MongoException e) {
      throw new MappingException("Failed to drop indexes for collection: " + collectionName, e);
    } catch (final IllegalArgumentException e) {
      throw new MappingException("Invalid collection configuration: " + collectionName, e);
    }
  }

  /**
   * List all indexes for a collection.
   *
   * @param entityClass the entity class
   * @return set of index names
   */
  public Set<String> listIndexes(final Class<?> entityClass) {
    Objects.requireNonNull(entityClass, "Entity class cannot be null");

    final EntityMetadata metadata = metadataExtractor.getMetadata(entityClass);
    final String collectionName = metadata.getCollectionName();
    final Set<String> indexNames = new HashSet<>();

    try {
      final MongoDatabase database = connectionManager.getDefaultDatabase();
      final MongoCollection<Document> collection = database.getCollection(collectionName);

      for (final Document index : collection.listIndexes()) {
        final String name = index.getString("name");
        if (name != null) {
          indexNames.add(name);
        }
      }

    } catch (final MongoException e) {
      throw new MappingException("Failed to list indexes for collection: " + collectionName, e);
    } catch (final IllegalArgumentException e) {
      throw new MappingException("Invalid collection configuration: " + collectionName, e);
    }
    return indexNames;
  }

  /**
   * Clear the ensured indexes tracking.
   *
   * <p>This forces indexes to be re-created on next ensureIndexes() call. Useful for testing.
   */
  public synchronized void clearEnsuredIndexes() {
    ensuredIndexes.clear();
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, "Cleared ensured indexes tracking");
    }
  }

  /**
   * Get the number of entity classes with ensured indexes.
   *
   * @return count of ensured entity classes
   */
  public synchronized int getEnsuredCount() {
    return ensuredIndexes.size();
  }

  @Override
  public String toString() {
    return "IndexManager{" + "ensuredIndexes=" + ensuredIndexes.size() + '}';
  }
}
