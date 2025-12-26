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
 * Immutable context for field deserialization.
 *
 * <p>This class holds all information needed to deserialize a single field from a document,
 * including the field itself, the document value, the target object, and metadata.
 *
 * <p><b>Thread Safety:</b> This class is immutable and thread-safe. All fields are final.
 *
 * <p><b>Design Pattern:</b> Context Object - encapsulates all data needed for deserialization.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class FieldDeserializationContext {

  private final Field field;
  private final Object value;
  private final Object target;
  private final Map<String, Object> document;
  private final EntityMetadata metadata;

  /**
   * Creates a new field deserialization context.
   *
   * @param field the field to deserialize into (must not be null)
   * @param value the value from document (may be null)
   * @param target the target object to set field on (must not be null)
   * @param document the source document map (must not be null)
   * @param metadata the entity metadata (must not be null)
   * @throws NullPointerException if any required parameter is null
   */
  public FieldDeserializationContext(
      final Field field,
      final Object value,
      final Object target,
      final Map<String, Object> document,
      final EntityMetadata metadata) {
    this.field = Objects.requireNonNull(field, "Field cannot be null");
    this.value = value;
    this.target = Objects.requireNonNull(target, "Target cannot be null");
    this.document = Objects.requireNonNull(document, "Document cannot be null");
    this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
  }

  /**
   * Get the field being deserialized.
   *
   * @return the field (never null)
   */
  public Field getField() {
    return field;
  }

  /**
   * Get the value from document.
   *
   * @return the value (may be null)
   */
  public Object getValue() {
    return value;
  }

  /**
   * Get the target object.
   *
   * @return the target object (never null)
   */
  public Object getTarget() {
    return target;
  }

  /**
   * Get the source document map.
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
   * Check if field type is primitive.
   *
   * @return true if field type is primitive
   */
  public boolean isFieldPrimitive() {
    return field.getType().isPrimitive();
  }

  /**
   * Check if field type is enum.
   *
   * @return true if field type is enum
   */
  public boolean isFieldEnum() {
    return field.getType().isEnum();
  }

  /**
   * Set field value on target object.
   *
   * <p>This method makes the field accessible before setting the value.
   *
   * @param val the value to set (may be null)
   * @throws IllegalAccessException if field cannot be accessed
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  public void setFieldValue(final Object val) throws IllegalAccessException {
    field.setAccessible(true);
    field.set(target, val);
  }

  /**
   * Get value from document with custom key.
   *
   * @param key the key to look up
   * @return the value, or null if not present
   */
  public Object getFromDocument(final String key) {
    return document.get(key);
  }

  /**
   * Check if document contains a specific key.
   *
   * @param key the key to check
   * @return true if document contains the key
   */
  public boolean documentContains(final String key) {
    return document.containsKey(key);
  }

  @Override
  public String toString() {
    return "FieldDeserializationContext{"
        + "field="
        + field.getName()
        + ", fieldType="
        + field.getType().getSimpleName()
        + ", valueNull="
        + isValueNull()
        + ", targetClass="
        + target.getClass().getSimpleName()
        + '}';
  }
}
