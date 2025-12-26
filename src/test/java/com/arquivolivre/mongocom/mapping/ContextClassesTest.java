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

import static org.junit.Assert.*;

import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for FieldSerializationContext and FieldDeserializationContext.
 *
 * <p>Tests the immutable context objects used in the serialization/deserialization chain.
 */
public class ContextClassesTest {

  private static class TestEntity {
    @Id private String id;

    @Reference private TestEntity reference;

    private String name;
    private int age;
    private boolean active;

    public enum Status {
      ACTIVE,
      INACTIVE
    }

    private Status status;
  }

  private Field idField;
  private Field nameField;
  private Field ageField;
  private Field activeField;
  private Field referenceField;
  private Field statusField;
  private EntityMetadata metadata;
  private TestEntity entity;
  private Map<String, Object> document;

  @Before
  public void setUp() throws Exception {
    idField = TestEntity.class.getDeclaredField("id");
    nameField = TestEntity.class.getDeclaredField("name");
    ageField = TestEntity.class.getDeclaredField("age");
    activeField = TestEntity.class.getDeclaredField("active");
    referenceField = TestEntity.class.getDeclaredField("reference");
    statusField = TestEntity.class.getDeclaredField("status");

    metadata =
        EntityMetadata.builder()
            .collectionName("test_collection")
            .entityClass(TestEntity.class)
            .idField(idField)
            .build();

    entity = new TestEntity();
    entity.id = "123";
    entity.name = "Test";
    entity.age = 25;
    entity.active = true;

    document = new HashMap<>();
  }

  // ========== FieldSerializationContext Tests ==========

  @Test
  public void testSerializationContextConstructor() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    assertNotNull(context);
    assertEquals(nameField, context.getField());
    assertEquals("Test", context.getValue());
    assertEquals(entity, context.getEntity());
    assertEquals(document, context.getDocument());
    assertEquals(metadata, context.getMetadata());
  }

  @Test(expected = NullPointerException.class)
  public void testSerializationContextNullField() {
    new FieldSerializationContext(null, "value", entity, document, metadata);
  }

  @Test(expected = NullPointerException.class)
  public void testSerializationContextNullEntity() {
    new FieldSerializationContext(nameField, "value", null, document, metadata);
  }

  @Test(expected = NullPointerException.class)
  public void testSerializationContextNullDocument() {
    new FieldSerializationContext(nameField, "value", entity, null, metadata);
  }

  @Test(expected = NullPointerException.class)
  public void testSerializationContextNullMetadata() {
    new FieldSerializationContext(nameField, "value", entity, document, null);
  }

  @Test
  public void testSerializationContextNullValue() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, null, entity, document, metadata);

    assertNull(context.getValue());
    assertTrue(context.isValueNull());
  }

  @Test
  public void testSerializationContextGetFieldName() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    assertEquals("name", context.getFieldName());
  }

  @Test
  public void testSerializationContextGetFieldType() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    assertEquals(String.class, context.getFieldType());
  }

  @Test
  public void testSerializationContextHasAnnotation() {
    final FieldSerializationContext idContext =
        new FieldSerializationContext(idField, "123", entity, document, metadata);
    final FieldSerializationContext nameContext =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    assertTrue(idContext.hasAnnotation(Id.class));
    assertFalse(nameContext.hasAnnotation(Id.class));
  }

  @Test
  public void testSerializationContextIsValueNull() {
    final FieldSerializationContext nullContext =
        new FieldSerializationContext(nameField, null, entity, document, metadata);
    final FieldSerializationContext nonNullContext =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    assertTrue(nullContext.isValueNull());
    assertFalse(nonNullContext.isValueNull());
  }

  @Test
  public void testSerializationContextIsValueEmptyString() {
    final FieldSerializationContext emptyContext =
        new FieldSerializationContext(nameField, "", entity, document, metadata);
    final FieldSerializationContext nonEmptyContext =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);
    final FieldSerializationContext nullContext =
        new FieldSerializationContext(nameField, null, entity, document, metadata);

    assertTrue(emptyContext.isValueEmptyString());
    assertFalse(nonEmptyContext.isValueEmptyString());
    assertFalse(nullContext.isValueEmptyString());
  }

  @Test
  public void testSerializationContextPutInDocument() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    context.putInDocument("TestValue");

    assertEquals("TestValue", document.get("name"));
  }

  @Test
  public void testSerializationContextPutInDocumentWithKey() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    context.putInDocument("customKey", "CustomValue");

    assertEquals("CustomValue", document.get("customKey"));
  }

  @Test(expected = NullPointerException.class)
  public void testSerializationContextPutInDocumentNullKey() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    context.putInDocument(null, "value");
  }

  @Test
  public void testSerializationContextIsAlreadyInDocument() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    assertFalse(context.isAlreadyInDocument());

    document.put("name", "existing");
    assertTrue(context.isAlreadyInDocument());
  }

  @Test
  public void testSerializationContextToString() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    final String str = context.toString();

    assertTrue(str.contains("FieldSerializationContext"));
    assertTrue(str.contains("field=name"));
    assertTrue(str.contains("fieldType=String"));
    assertTrue(str.contains("valueNull=false"));
    assertTrue(str.contains("entityClass=TestEntity"));
  }

  @Test
  public void testSerializationContextToStringWithNullValue() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, null, entity, document, metadata);

    final String str = context.toString();

    assertTrue(str.contains("valueNull=true"));
  }

  // ========== FieldDeserializationContext Tests ==========

  @Test
  public void testDeserializationContextConstructor() {
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertNotNull(context);
    assertEquals(nameField, context.getField());
    assertEquals("Test", context.getValue());
    assertEquals(entity, context.getTarget());
    assertEquals(document, context.getDocument());
    assertEquals(metadata, context.getMetadata());
  }

  @Test(expected = NullPointerException.class)
  public void testDeserializationContextNullField() {
    new FieldDeserializationContext(null, "value", entity, document, metadata);
  }

  @Test(expected = NullPointerException.class)
  public void testDeserializationContextNullTarget() {
    new FieldDeserializationContext(nameField, "value", null, document, metadata);
  }

  @Test(expected = NullPointerException.class)
  public void testDeserializationContextNullDocument() {
    new FieldDeserializationContext(nameField, "value", entity, null, metadata);
  }

  @Test(expected = NullPointerException.class)
  public void testDeserializationContextNullMetadata() {
    new FieldDeserializationContext(nameField, "value", entity, document, null);
  }

  @Test
  public void testDeserializationContextNullValue() {
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, null, entity, document, metadata);

    assertNull(context.getValue());
    assertTrue(context.isValueNull());
  }

  @Test
  public void testDeserializationContextGetFieldName() {
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertEquals("name", context.getFieldName());
  }

  @Test
  public void testDeserializationContextGetFieldType() {
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertEquals(String.class, context.getFieldType());
  }

  @Test
  public void testDeserializationContextHasAnnotation() {
    final FieldDeserializationContext idContext =
        new FieldDeserializationContext(idField, "123", entity, document, metadata);
    final FieldDeserializationContext nameContext =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertTrue(idContext.hasAnnotation(Id.class));
    assertFalse(nameContext.hasAnnotation(Id.class));
  }

  @Test
  public void testDeserializationContextHasReferenceAnnotation() {
    final FieldDeserializationContext refContext =
        new FieldDeserializationContext(referenceField, null, entity, document, metadata);

    assertTrue(refContext.hasAnnotation(Reference.class));
  }

  @Test
  public void testDeserializationContextIsValueNull() {
    final FieldDeserializationContext nullContext =
        new FieldDeserializationContext(nameField, null, entity, document, metadata);
    final FieldDeserializationContext nonNullContext =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertTrue(nullContext.isValueNull());
    assertFalse(nonNullContext.isValueNull());
  }

  @Test
  public void testDeserializationContextIsFieldPrimitive() {
    final FieldDeserializationContext stringContext =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);
    final FieldDeserializationContext intContext =
        new FieldDeserializationContext(ageField, 25, entity, document, metadata);
    final FieldDeserializationContext boolContext =
        new FieldDeserializationContext(activeField, true, entity, document, metadata);

    assertFalse(stringContext.isFieldPrimitive());
    assertTrue(intContext.isFieldPrimitive());
    assertTrue(boolContext.isFieldPrimitive());
  }

  @Test
  public void testDeserializationContextIsFieldEnum() {
    final FieldDeserializationContext stringContext =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);
    final FieldDeserializationContext enumContext =
        new FieldDeserializationContext(
            statusField, TestEntity.Status.ACTIVE, entity, document, metadata);

    assertFalse(stringContext.isFieldEnum());
    assertTrue(enumContext.isFieldEnum());
  }

  @Test
  public void testDeserializationContextSetFieldValue() throws Exception {
    final TestEntity target = new TestEntity();
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "NewName", target, document, metadata);

    context.setFieldValue("NewName");

    nameField.setAccessible(true);
    assertEquals("NewName", nameField.get(target));
  }

  @Test
  public void testDeserializationContextSetFieldValueNull() throws Exception {
    final TestEntity target = new TestEntity();
    target.name = "OldName";
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, null, target, document, metadata);

    context.setFieldValue(null);

    nameField.setAccessible(true);
    assertNull(nameField.get(target));
  }

  @Test
  public void testDeserializationContextSetFieldValuePrimitive() throws Exception {
    final TestEntity target = new TestEntity();
    final FieldDeserializationContext context =
        new FieldDeserializationContext(ageField, 30, target, document, metadata);

    context.setFieldValue(30);

    ageField.setAccessible(true);
    assertEquals(30, ageField.get(target));
  }

  @Test
  public void testDeserializationContextGetFromDocument() {
    document.put("testKey", "testValue");
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertEquals("testValue", context.getFromDocument("testKey"));
    assertNull(context.getFromDocument("nonExistentKey"));
  }

  @Test
  public void testDeserializationContextDocumentContains() {
    document.put("existingKey", "value");
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    assertTrue(context.documentContains("existingKey"));
    assertFalse(context.documentContains("nonExistentKey"));
  }

  @Test
  public void testDeserializationContextToString() {
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);

    final String str = context.toString();

    assertTrue(str.contains("FieldDeserializationContext"));
    assertTrue(str.contains("field=name"));
    assertTrue(str.contains("fieldType=String"));
    assertTrue(str.contains("valueNull=false"));
    assertTrue(str.contains("targetClass=TestEntity"));
  }

  @Test
  public void testDeserializationContextToStringWithNullValue() {
    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, null, entity, document, metadata);

    final String str = context.toString();

    assertTrue(str.contains("valueNull=true"));
  }

  // ========== Edge Cases and Integration Tests ==========

  @Test
  public void testSerializationContextWithComplexValue() {
    final Map<String, Object> complexValue = new HashMap<>();
    complexValue.put("nested", "value");

    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, complexValue, entity, document, metadata);

    assertEquals(complexValue, context.getValue());
    assertFalse(context.isValueNull());
    assertFalse(context.isValueEmptyString());
  }

  @Test
  public void testDeserializationContextWithComplexValue() {
    final Map<String, Object> complexValue = new HashMap<>();
    complexValue.put("nested", "value");

    final FieldDeserializationContext context =
        new FieldDeserializationContext(nameField, complexValue, entity, document, metadata);

    assertEquals(complexValue, context.getValue());
    assertFalse(context.isValueNull());
  }

  @Test
  public void testSerializationContextMultiplePuts() {
    final FieldSerializationContext context =
        new FieldSerializationContext(nameField, "Test", entity, document, metadata);

    context.putInDocument("key1", "value1");
    context.putInDocument("key2", "value2");
    context.putInDocument("value3");

    assertEquals("value1", document.get("key1"));
    assertEquals("value2", document.get("key2"));
    assertEquals("value3", document.get("name"));
    assertEquals(3, document.size());
  }

  @Test
  public void testDeserializationContextMultipleFieldTypes() {
    final FieldDeserializationContext stringCtx =
        new FieldDeserializationContext(nameField, "Test", entity, document, metadata);
    final FieldDeserializationContext intCtx =
        new FieldDeserializationContext(ageField, 25, entity, document, metadata);
    final FieldDeserializationContext boolCtx =
        new FieldDeserializationContext(activeField, true, entity, document, metadata);

    assertEquals(String.class, stringCtx.getFieldType());
    assertEquals(int.class, intCtx.getFieldType());
    assertEquals(boolean.class, boolCtx.getFieldType());

    assertFalse(stringCtx.isFieldPrimitive());
    assertTrue(intCtx.isFieldPrimitive());
    assertTrue(boolCtx.isFieldPrimitive());
  }
}
