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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable metadata holder for entity classes.
 *
 * <p>This class caches expensive reflection operations performed during entity introspection. Once
 * created, instances are immutable and thread-safe.
 *
 * <p><b>Thread Safety:</b> This class is immutable and thread-safe. All fields are final and
 * collections are unmodifiable.
 *
 * <p><b>Performance:</b> Metadata extraction is expensive (reflection), so instances should be
 * cached and reused.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class EntityMetadata {

  private final Class<?> entityClass;
  private final String collectionName;
  private final Field idField;
  private final Field objectIdField;
  private final List<Field> indexedFields;
  private final List<Field> referenceFields;
  private final List<Field> internalFields;
  private final List<Field> generatedFields;

  /**
   * Private constructor - use Builder to create instances.
   *
   * @param builder the builder containing metadata
   */
  private EntityMetadata(final Builder builder) {
    this.entityClass = Objects.requireNonNull(builder.entityClass, "Entity class cannot be null");
    this.collectionName =
        Objects.requireNonNull(builder.collectionName, "Collection name cannot be null");
    this.idField = builder.idField;
    this.objectIdField = builder.objectIdField;
    this.indexedFields = Collections.unmodifiableList(builder.indexedFields);
    this.referenceFields = Collections.unmodifiableList(builder.referenceFields);
    this.internalFields = Collections.unmodifiableList(builder.internalFields);
    this.generatedFields = Collections.unmodifiableList(builder.generatedFields);
  }

  /**
   * Get the entity class.
   *
   * @return entity class (never null)
   */
  public Class<?> getEntityClass() {
    return entityClass;
  }

  /**
   * Get the MongoDB collection name.
   *
   * @return collection name (never null)
   */
  public String getCollectionName() {
    return collectionName;
  }

  /**
   * Get the field annotated with @Id.
   *
   * @return ID field, or null if not present
   */
  public Field getIdField() {
    return idField;
  }

  /**
   * Get the field annotated with @ObjectId.
   *
   * @return ObjectId field, or null if not present
   */
  public Field getObjectIdField() {
    return objectIdField;
  }

  /**
   * Get all fields annotated with @Index.
   *
   * @return unmodifiable list of indexed fields (never null, may be empty)
   */
  public List<Field> getIndexedFields() {
    return indexedFields;
  }

  /**
   * Get all fields annotated with @Reference.
   *
   * @return unmodifiable list of reference fields (never null, may be empty)
   */
  public List<Field> getReferenceFields() {
    return referenceFields;
  }

  /**
   * Get all fields annotated with @Internal.
   *
   * @return unmodifiable list of internal fields (never null, may be empty)
   */
  public List<Field> getInternalFields() {
    return internalFields;
  }

  /**
   * Get all fields annotated with @GeneratedValue.
   *
   * @return unmodifiable list of generated fields (never null, may be empty)
   */
  public List<Field> getGeneratedFields() {
    return generatedFields;
  }

  /**
   * Check if entity has an ID field.
   *
   * @return true if ID field is present
   */
  public boolean hasIdField() {
    return idField != null;
  }

  /**
   * Check if entity has an ObjectId field.
   *
   * @return true if ObjectId field is present
   */
  public boolean hasObjectIdField() {
    return objectIdField != null;
  }

  /**
   * Check if entity has any indexed fields.
   *
   * @return true if at least one indexed field exists
   */
  public boolean hasIndexes() {
    return !indexedFields.isEmpty();
  }

  /**
   * Check if entity has any reference fields.
   *
   * @return true if at least one reference field exists
   */
  public boolean hasReferences() {
    return !referenceFields.isEmpty();
  }

  /**
   * Check if entity has any internal fields.
   *
   * @return true if at least one internal field exists
   */
  public boolean hasInternalFields() {
    return !internalFields.isEmpty();
  }

  /**
   * Check if entity has any generated fields.
   *
   * @return true if at least one generated field exists
   */
  public boolean hasGeneratedFields() {
    return !generatedFields.isEmpty();
  }

  @Override
  public String toString() {
    return "EntityMetadata{"
        + "entityClass="
        + entityClass.getName()
        + ", collectionName='"
        + collectionName
        + '\''
        + ", hasIdField="
        + hasIdField()
        + ", hasObjectIdField="
        + hasObjectIdField()
        + ", indexedFields="
        + indexedFields.size()
        + ", referenceFields="
        + referenceFields.size()
        + ", internalFields="
        + internalFields.size()
        + ", generatedFields="
        + generatedFields.size()
        + '}';
  }

  /**
   * Create a new builder for EntityMetadata.
   *
   * @return new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for EntityMetadata.
   *
   * <p>Provides a fluent API for constructing EntityMetadata instances.
   */
  public static final class Builder {
    private Class<?> entityClass;
    private String collectionName;
    private Field idField;
    private Field objectIdField;
    private List<Field> indexedFields = Collections.emptyList();
    private List<Field> referenceFields = Collections.emptyList();
    private List<Field> internalFields = Collections.emptyList();
    private List<Field> generatedFields = Collections.emptyList();

    private Builder() {}

    /**
     * Set the entity class.
     *
     * @param entityClass the entity class (must not be null)
     * @return this builder
     */
    public Builder entityClass(final Class<?> entityClass) {
      this.entityClass = entityClass;
      return this;
    }

    /**
     * Set the collection name.
     *
     * @param collectionName the collection name (must not be null)
     * @return this builder
     */
    public Builder collectionName(final String collectionName) {
      this.collectionName = collectionName;
      return this;
    }

    /**
     * Set the ID field.
     *
     * @param idField the ID field (may be null)
     * @return this builder
     */
    public Builder idField(final Field idField) {
      this.idField = idField;
      return this;
    }

    /**
     * Set the ObjectId field.
     *
     * @param objectIdField the ObjectId field (may be null)
     * @return this builder
     */
    public Builder objectIdField(final Field objectIdField) {
      this.objectIdField = objectIdField;
      return this;
    }

    /**
     * Set the indexed fields.
     *
     * @param indexedFields list of indexed fields (must not be null)
     * @return this builder
     */
    public Builder indexedFields(final List<Field> indexedFields) {
      this.indexedFields = Objects.requireNonNull(indexedFields, "Indexed fields cannot be null");
      return this;
    }

    /**
     * Set the reference fields.
     *
     * @param referenceFields list of reference fields (must not be null)
     * @return this builder
     */
    public Builder referenceFields(final List<Field> referenceFields) {
      this.referenceFields =
          Objects.requireNonNull(referenceFields, "Reference fields cannot be null");
      return this;
    }

    /**
     * Set the internal fields.
     *
     * @param internalFields list of internal fields (must not be null)
     * @return this builder
     */
    public Builder internalFields(final List<Field> internalFields) {
      this.internalFields =
          Objects.requireNonNull(internalFields, "Internal fields cannot be null");
      return this;
    }

    /**
     * Set the generated fields.
     *
     * @param generatedFields list of generated fields (must not be null)
     * @return this builder
     */
    public Builder generatedFields(final List<Field> generatedFields) {
      this.generatedFields =
          Objects.requireNonNull(generatedFields, "Generated fields cannot be null");
      return this;
    }

    /**
     * Build the EntityMetadata instance.
     *
     * @return new EntityMetadata instance
     * @throws NullPointerException if required fields are null
     */
    public EntityMetadata build() {
      return new EntityMetadata(this);
    }
  }
}
