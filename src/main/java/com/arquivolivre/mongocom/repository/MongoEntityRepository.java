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

package com.arquivolivre.mongocom.repository;

import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.management.MongoQuery;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import com.arquivolivre.mongocom.mapping.FieldDeserializer;
import com.arquivolivre.mongocom.mapping.FieldDeserializerChain;
import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.mapping.FieldSerializer;
import com.arquivolivre.mongocom.mapping.FieldSerializerChain;
import com.arquivolivre.mongocom.mapping.deserializers.DefaultDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.EnumDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.InternalFieldDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.ListDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.NestedObjectDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.NullValueDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.ObjectIdDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.PrimitiveDeserializer;
import com.arquivolivre.mongocom.mapping.deserializers.ReferenceDeserializer;
import com.arquivolivre.mongocom.mapping.serializers.EnumSerializer;
import com.arquivolivre.mongocom.mapping.serializers.InternalFieldSerializer;
import com.arquivolivre.mongocom.mapping.serializers.ListSerializer;
import com.arquivolivre.mongocom.mapping.serializers.NestedObjectSerializer;
import com.arquivolivre.mongocom.mapping.serializers.NullValueSerializer;
import com.arquivolivre.mongocom.mapping.serializers.ObjectIdSerializer;
import com.arquivolivre.mongocom.mapping.serializers.PrimitiveSerializer;
import com.arquivolivre.mongocom.mapping.serializers.ReferenceSerializer;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * MongoDB implementation of EntityRepository.
 *
 * <p>This class provides a complete implementation of the Repository pattern for MongoDB,
 * integrating with all the refactored components:
 *
 * <ul>
 *   <li>MongoConnectionManager for thread-safe connections
 *   <li>EntityMetadataExtractor for entity metadata
 *   <li>DocumentMapper for serialization/deserialization
 *   <li>FieldSerializerChain and FieldDeserializerChain
 * </ul>
 *
 * <p><b>Design Pattern:</b> Repository - encapsulates data access logic
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. All operations use thread-safe components.
 *
 * <p><b>Generic Types:</b>
 *
 * <ul>
 *   <li>T - the entity type
 *   <li>I - the entity I type (typically String)
 * </ul>
 *
 * @param <T> the entity type
 * @param <I> the entity I type
 * @author MongOCOM Team
 * @since 0.5
 */
public class MongoEntityRepository<T, I> implements EntityRepository<T, I> {

  private static final Logger LOGGER = Logger.getLogger(MongoEntityRepository.class.getName());

  private final Class<T> entityClass;
  private final MongoConnectionManager connectionManager;
  private final EntityMetadata metadata;
  private final FieldSerializerChain serializerChain;
  private FieldDeserializerChain deserializerChain;

  /**
   * Creates a new MongoDB entity repository.
   *
   * @param entityClass the entity class (must not be null)
   * @param connectionManager the connection manager (must not be null)
   * @throws NullPointerException if any parameter is null
   * @throws MappingException if entity metadata cannot be extracted
   */
  public MongoEntityRepository(
      final Class<T> entityClass, final MongoConnectionManager connectionManager) {
    this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    this.connectionManager =
        Objects.requireNonNull(connectionManager, "Connection manager cannot be null");

    // Create extractor instance and get metadata
    final EntityMetadataExtractor extractor = new EntityMetadataExtractor();
    this.metadata = extractor.getMetadata(entityClass);

    // Initialize serializer chain with all available serializers
    this.serializerChain = createDefaultSerializerChain();

    // Initialize deserializer chain with all available deserializers (without repository factory)
    this.deserializerChain = createDefaultDeserializerChain(null);

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(
          Level.FINE,
          "Created repository for entity {0}, collection {1}",
          new Object[] {entityClass.getName(), metadata.getCollectionName()});
    }
  }

  /**
   * Set the repository factory for reference resolution.
   *
   * <p>This method is called by RepositoryFactory after creating the repository to enable reference
   * resolution.
   *
   * @param repositoryFactory the repository factory (may be null to disable reference resolution)
   */
  public void setRepositoryFactory(final RepositoryFactory repositoryFactory) {
    // Recreate deserializer chain with repository factory
    this.deserializerChain = createDefaultDeserializerChain(repositoryFactory);
  }

  @Override
  public long count() {
    final MongoCollection<Document> collection = getCollection();
    return collection.countDocuments();
  }

  @Override
  public long count(final MongoQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Bson filter = query.getQuery();
    return collection.countDocuments(filter);
  }

  @Override
  public List<T> findAll() {
    final MongoCollection<Document> collection = getCollection();
    final List<T> results = new ArrayList<>();

    for (final Document doc : collection.find()) {
      final T entity = deserializeDocument(doc);
      results.add(entity);
    }

    return results;
  }

  @Override
  public List<T> find(final MongoQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Bson filter = query.getQuery();
    final List<T> results = new ArrayList<>();

    for (final Document doc : collection.find(filter)) {
      final T entity = deserializeDocument(doc);
      results.add(entity);
    }

    return results;
  }

  @Override
  public Optional<T> findOne(final MongoQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Bson filter = query.getQuery();
    final Document doc = collection.find(filter).first();

    final Optional<T> result;
    if (doc == null) {
      result = Optional.empty();
    } else {
      final T entity = deserializeDocument(doc);
      result = Optional.of(entity);
    }

    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<T> findById(final I entityId) {
    Objects.requireNonNull(entityId, "I cannot be null");
    final MongoCollection<Document> collection = getCollection();

    // Convert String ID to ObjectId if necessary
    Object queryId = entityId;
    if (entityId instanceof String) {
      try {
        queryId = new org.bson.types.ObjectId((String) entityId);
      } catch (final IllegalArgumentException e) {
        // Not a valid ObjectId format, use as-is
        queryId = entityId;
      }
    }

    final Document doc = collection.find(new Document("_id", queryId)).first();

    final Optional<T> result;
    if (doc == null) {
      result = Optional.empty();
    } else {
      final T entity = deserializeDocument(doc);
      result = Optional.of(entity);
    }

    return result;
  }

  @Override
  public boolean existsById(final I entityId) {
    Objects.requireNonNull(entityId, "I cannot be null");
    final MongoCollection<Document> collection = getCollection();
    return collection.countDocuments(new Document("_id", entityId)) > 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public I insert(final T entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Document doc = serializeEntity(entity);

    collection.insertOne(doc);

    // Extract the generated _id from the document
    final Object idValue = doc.get("_id");

    // Set the ID back on the entity object
    if (idValue != null && metadata.getObjectIdField() != null) {
      try {
        final Field idField = metadata.getObjectIdField();
        idField.setAccessible(true);
        idField.set(entity, idValue.toString());
      } catch (final IllegalAccessException e) {
        LOGGER.log(Level.WARNING, "Failed to set generated ID on entity: {0}", e.getMessage());
      }
    }

    return (I) idValue;
  }

  @Override
  public long update(final MongoQuery query, final T entity) {
    Objects.requireNonNull(query, "Query cannot be null");
    Objects.requireNonNull(entity, "Entity cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Bson filter = query.getQuery();
    final Document updateDoc = serializeEntity(entity);

    // Remove _id from update document (cannot update _id)
    updateDoc.remove("_id");

    final UpdateResult result = collection.updateOne(filter, new Document("$set", updateDoc));
    return result.getModifiedCount();
  }

  @Override
  public long updateMulti(final MongoQuery query, final T entity) {
    Objects.requireNonNull(query, "Query cannot be null");
    Objects.requireNonNull(entity, "Entity cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Bson filter = query.getQuery();
    final Document updateDoc = serializeEntity(entity);

    // Remove _id from update document
    updateDoc.remove("_id");

    final UpdateResult result = collection.updateMany(filter, new Document("$set", updateDoc));
    return result.getModifiedCount();
  }

  @Override
  @SuppressWarnings("unchecked")
  public I save(final T entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");

    final I result;

    // Check if entity has an I
    final Field idField = metadata.getIdField();
    if (idField == null) {
      // No I field, just insert
      result = insert(entity);
    } else {
      try {
        idField.setAccessible(true);
        final Object idValue = idField.get(entity);

        if (idValue == null) {
          // No I value, insert
          result = insert(entity);
        } else {
          // Check if entity exists
          if (existsById((I) idValue)) {
            // Update existing
            final MongoQuery query = new MongoQuery().add("_id", idValue);
            update(query, entity);
            result = (I) idValue;
          } else {
            // Insert new
            result = insert(entity);
          }
        }
      } catch (final IllegalAccessException e) {
        throw new MappingException("Failed to access I field for save operation", e);
      }
    }

    return result;
  }

  @Override
  public boolean delete(final T entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");

    // Extract I from entity
    final Field idField = metadata.getIdField();
    if (idField == null) {
      throw new MappingException("Cannot delete entity without @Id field");
    }

    final boolean result;
    try {
      idField.setAccessible(true);
      final Object idValue = idField.get(entity);

      if (idValue == null) {
        result = false;
      } else {
        @SuppressWarnings("unchecked")
        final I id = (I) idValue;
        result = deleteById(id);
      }
    } catch (final IllegalAccessException e) {
      throw new MappingException("Failed to access I field for delete operation", e);
    }

    return result;
  }

  @Override
  public long delete(final MongoQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    final MongoCollection<Document> collection = getCollection();
    final Bson filter = query.getQuery();
    final DeleteResult result = collection.deleteMany(filter);
    return result.getDeletedCount();
  }

  @Override
  public boolean deleteById(final I entityId) {
    Objects.requireNonNull(entityId, "I cannot be null");
    final MongoCollection<Document> collection = getCollection();

    // Use the @Id field name, not "_id"
    final Field idField = metadata.getIdField();
    final String idFieldName = idField != null ? idField.getName() : "_id";

    final DeleteResult result = collection.deleteOne(new Document(idFieldName, entityId));
    return result.getDeletedCount() > 0;
  }

  @Override
  public long deleteAll() {
    final MongoCollection<Document> collection = getCollection();
    final DeleteResult result = collection.deleteMany(new Document());
    return result.getDeletedCount();
  }

  @Override
  public Class<T> getEntityClass() {
    return entityClass;
  }

  @Override
  public String getCollectionName() {
    return metadata.getCollectionName();
  }

  /**
   * Get the MongoDB collection for this entity type.
   *
   * @return the collection (never null)
   */
  private MongoCollection<Document> getCollection() {
    final MongoDatabase database = connectionManager.getDefaultDatabase();
    return database.getCollection(metadata.getCollectionName());
  }

  /**
   * Serialize entity to Document.
   *
   * @param entity the entity to serialize
   * @return the document
   * @throws MappingException if serialization fails
   */
  private Document serializeEntity(final T entity) {
    final Document doc = new Document();

    // Get all fields from the entity class
    final List<Field> fields = getAllFields(entity.getClass());

    for (final Field field : fields) {
      field.setAccessible(true);

      try {
        final Object value = field.get(entity);

        // Create serialization context
        final FieldSerializationContext context =
            new FieldSerializationContext(field, value, entity, doc, metadata);

        // Use chain to serialize
        serializerChain.serialize(context);

      } catch (final IllegalAccessException e) {
        throw new MappingException("Failed to access field: " + field.getName(), e);
      }
    }

    return doc;
  }

  /**
   * Deserialize Document to entity.
   *
   * @param doc the document to deserialize
   * @return the entity
   * @throws MappingException if deserialization fails
   */
  private T deserializeDocument(final Document doc) {
    try {
      // Create new instance of entity
      final T entity = entityClass.getDeclaredConstructor().newInstance();

      // Get all fields from the entity class
      final List<Field> fields = getAllFields(entityClass);

      for (final Field field : fields) {
        field.setAccessible(true);

        // Get value from document (use field name or @Id annotation)
        final String fieldName = field.getName();
        final Object value = doc.get(fieldName);

        // Create deserialization context
        final FieldDeserializationContext context =
            new FieldDeserializationContext(field, value, entity, doc, metadata);

        // Use chain to deserialize
        deserializerChain.deserialize(context);
      }

      return entity;

    } catch (final ReflectiveOperationException e) {
      throw new MappingException("Failed to create instance of " + entityClass.getName(), e);
    }
  }

  /**
   * Get all fields from class hierarchy.
   *
   * @param clazz the class to inspect
   * @return list of all fields including inherited ones
   */
  private List<Field> getAllFields(final Class<?> clazz) {
    final List<Field> fields = new ArrayList<>();
    Class<?> current = clazz;

    while (current != null && current != Object.class) {
      fields.addAll(Arrays.asList(current.getDeclaredFields()));
      current = current.getSuperclass();
    }

    return fields;
  }

  /**
   * Create default serializer chain with all available serializers.
   *
   * <p>Serializers are added in priority order (lower priority number = higher priority):
   *
   * <ul>
   *   <li>Priority 0: NullValueSerializer - skips null values
   *   <li>Priority 5: InternalFieldSerializer - skips @Internal fields
   *   <li>Priority 10: ObjectIdSerializer - handles @ObjectId as "_id"
   *   <li>Priority 15: ReferenceSerializer - stores only ObjectId for @Reference
   *   <li>Priority 20: ListSerializer - handles List fields with nested support
   *   <li>Priority 25: NestedObjectSerializer - handles complex nested objects
   *   <li>Priority 30: EnumSerializer - converts enums to String
   *   <li>Priority 35: PrimitiveSerializer - handles primitives and wrappers
   *   <li>Priority 100: DefaultFieldSerializer - fallback for any remaining types
   * </ul>
   *
   * @return configured serializer chain
   */
  private FieldSerializerChain createDefaultSerializerChain() {
    return FieldSerializerChain.builder()
        .add(new NullValueSerializer())
        .add(new InternalFieldSerializer())
        .add(new ObjectIdSerializer())
        .add(new ReferenceSerializer())
        .add(new ListSerializer())
        .add(new NestedObjectSerializer())
        .add(new EnumSerializer())
        .add(new PrimitiveSerializer())
        .add(new DefaultFieldSerializer())
        .build();
  }

  /**
   * Create default deserializer chain with all available deserializers.
   *
   * @param repositoryFactory the repository factory for reference resolution (may be null)
   * @return configured deserializer chain
   */
  private FieldDeserializerChain createDefaultDeserializerChain(
      final RepositoryFactory repositoryFactory) {
    final List<FieldDeserializer> deserializers =
        Arrays.asList(
            new NullValueDeserializer(),
            new InternalFieldDeserializer(),
            new ObjectIdDeserializer(),
            repositoryFactory != null
                ? new ReferenceDeserializer(repositoryFactory)
                : new ReferenceDeserializer(),
            new PrimitiveDeserializer(),
            new EnumDeserializer(),
            new ListDeserializer(),
            new NestedObjectDeserializer(),
            new DefaultDeserializer());

    return new FieldDeserializerChain(deserializers);
  }

  /** Default field serializer that handles basic types. */
  private static final class DefaultFieldSerializer implements FieldSerializer {
    @Override
    public boolean canHandle(final FieldSerializationContext context) {
      return true; // Handles everything as fallback
    }

    @Override
    public void serialize(final FieldSerializationContext context) {
      final Object value = context.getValue();
      if (value != null) {
        context.putInDocument(value);
      }
    }

    @Override
    public int getPriority() {
      return Integer.MAX_VALUE; // Lowest priority (last resort)
    }

    @Override
    public String toString() {
      return "DefaultFieldSerializer";
    }
  }

  @Override
  public String toString() {
    return "MongoEntityRepository{"
        + "entityClass="
        + entityClass.getSimpleName()
        + ", collection="
        + metadata.getCollectionName()
        + '}';
  }
}
