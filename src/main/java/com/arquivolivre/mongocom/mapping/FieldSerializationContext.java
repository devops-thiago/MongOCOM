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

import com.arquivolivre.mongocom.metadata.EntityMetadata;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable context for field serialization.
 *
 * <p>This class holds all information needed to serialize a single field, including the field
 * itself, its value, the target document, and metadata about the entity.
 *
 * <p><b>Thread Safety:</b> This class is immutable and thread-safe. All fields are final.
 *
 * <p><b>Design Pattern:</b> Context Object - encapsulates all data needed for an operation.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class FieldSerializationContext {

  private final Field field;
  private final Object value;
  private final Object entity;
  private final Map<String, Object> document;
  private final EntityMetadata metadata;

  /**
   * Creates a new field serialization context.
   *
   * @param field the field to serialize (must not be null)
   * @param value the field value (may be null)
   * @param entity the entity containing the field (must not be null)
   * @param document the target document map (must not be null)
   * @param metadata the entity metadata (must not be null)
   * @throws NullPointerException if any required parameter is null
   */
  public FieldSerializationContext(
      final Field field,
      final Object value,
      final Object entity,
      final Map<String, Object> document,
      final EntityMetadata metadata) {
    this.field = Objects.requireNonNull(field, "Field cannot be null");
    this.value = value;
    this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
    this.document = Objects.requireNonNull(document, "Document cannot be null");
    this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
  }

  /**
   * Get the field being serialized.
   *
   * @return the field (never null)
   */
  public Field getField() {
    return field;
  }

  /**
   * Get the field value.
   *
   * @return the field value (may be null)
   */
  public Object getValue() {
    return value;
  }

  /**
   * Get the entity containing the field.
   *
   * @return the entity (never null)
   */
  public Object getEntity() {
    return entity;
  }

  /**
   * Get the target document map.
   *
   * @return the document map (never null)
   */
  public Map<String, Object> getDocument() {
    return document;
  }

  /**
   * Get the entity metadata.
   *
   * @return the metadata (never null)
   */
  public EntityMetadata getMetadata() {
    return metadata;
  }

  /**
   * Check if field has specific annotation.
   *
   * @param annotationClass the annotation class to check
   * @return true if field has the annotation
   */
  public boolean hasAnnotation(final Class<? extends Annotation> annotationClass) {
    return field.isAnnotationPresent(annotationClass);
  }

  /**
   * Get field name.
   *
   * @return the field name (never null)
   */
  public String getFieldName() {
    return field.getName();
  }

  /**
   * Get field type.
   *
   * @return the field type (never null)
   */
  public Class<?> getFieldType() {
    return field.getType();
  }

  /**
   * Check if value is null.
   *
   * @return true if value is null
   */
  public boolean isValueNull() {
    return value == null;
  }

  /**
   * Check if value is empty string.
   *
   * @return true if value is empty string
   */
  public boolean isValueEmptyString() {
    return "".equals(value);
  }

  /**
   * Put value in document with field name as key.
   *
   * @param val the value to put (may be null)
   */
  public void putInDocument(final Object val) {
    document.put(getFieldName(), val);
  }

  /**
   * Put value in document with custom key.
   *
   * @param key the key (must not be null)
   * @param val the value (may be null)
   * @throws NullPointerException if key is null
   */
  public void putInDocument(final String key, final Object val) {
    Objects.requireNonNull(key, "Key cannot be null");
    document.put(key, val);
  }

  /**
   * Check if document already contains a value for this field.
   *
   * @return true if document contains the field
   */
  public boolean isAlreadyInDocument() {
    return document.containsKey(getFieldName());
  }

  @Override
  public String toString() {
    return "FieldSerializationContext{"
        + "field="
        + field.getName()
        + ", fieldType="
        + field.getType().getSimpleName()
        + ", valueNull="
        + isValueNull()
        + ", entityClass="
        + entity.getClass().getSimpleName()
        + '}';
  }
}
