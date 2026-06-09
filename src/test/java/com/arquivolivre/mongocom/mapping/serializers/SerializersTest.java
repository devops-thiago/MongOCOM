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

package com.arquivolivre.mongocom.mapping.serializers;

import static org.junit.Assert.*;

import com.arquivolivre.mongocom.mapping.FieldSerializationContext;
import com.arquivolivre.mongocom.metadata.EntityMetadata;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import com.arquivolivre.mongocom.testutil.TestEntities;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive tests for all field serializers.
 *
 * @author MongOCOM Team
 */
public class SerializersTest {

  private EntityMetadataExtractor metadataExtractor;
  private Map<String, Object> document;

  @Before
  public void setUp() {
    metadataExtractor = new EntityMetadataExtractor();
    document = new HashMap<>();
  }

  // ==================== NullValueSerializer Tests ====================

  @Test
  public void testNullValueSerializer_CanHandleNullValue() throws Exception {
    final NullValueSerializer serializer = new NullValueSerializer();
    final TestEntities.SimpleEntity entity = new TestEntities.SimpleEntity();
    entity.setName(null);

    final Field field = TestEntities.SimpleEntity.class.getDeclaredField("name");
    final EntityMetadata metadata = metadataExtractor.getMetadata(TestEntities.SimpleEntity.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, null, entity, document, metadata);

    assertTrue("Should handle null values", serializer.canHandle(context));
  }

  @Test
  public void testNullValueSerializer_SkipsNullValue() throws Exception {
    final NullValueSerializer serializer = new NullValueSerializer();
    final TestEntities.SimpleEntity entity = new TestEntities.SimpleEntity();
    entity.setName(null);

    final Field field = TestEntities.SimpleEntity.class.getDeclaredField("name");
    final EntityMetadata metadata = metadataExtractor.getMetadata(TestEntities.SimpleEntity.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, null, entity, document, metadata);

    serializer.serialize(context);

    assertFalse("Document should not contain null value", document.containsKey("name"));
  }

  @Test
  public void testNullValueSerializer_DoesNotHandleNonNullValue() throws Exception {
    final NullValueSerializer serializer = new NullValueSerializer();
    final TestEntities.SimpleEntity entity = new TestEntities.SimpleEntity();
    entity.setName("test");

    final Field field = TestEntities.SimpleEntity.class.getDeclaredField("name");
    final EntityMetadata metadata = metadataExtractor.getMetadata(TestEntities.SimpleEntity.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "test", entity, document, metadata);

    assertFalse("Should not handle non-null values", serializer.canHandle(context));
  }

  @Test
  public void testNullValueSerializer_Priority() {
    final NullValueSerializer serializer = new NullValueSerializer();
    assertEquals("Priority should be 0", 0, serializer.getPriority());
  }

  // ==================== InternalFieldSerializer Tests ====================

  @Test
  public void testInternalFieldSerializer_CanHandleInternalField() throws Exception {
    final InternalFieldSerializer serializer = new InternalFieldSerializer();
    final TestEntities.EntityWithInternal entity = new TestEntities.EntityWithInternal();

    final Field field = TestEntities.EntityWithInternal.class.getDeclaredField("internalField");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithInternal.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "secret", entity, document, metadata);

    assertTrue("Should handle @Internal fields", serializer.canHandle(context));
  }

  @Test
  public void testInternalFieldSerializer_SerializesInternalField() throws Exception {
    final InternalFieldSerializer serializer = new InternalFieldSerializer();
    final TestEntities.EntityWithInternal entity = new TestEntities.EntityWithInternal();

    final Field field = TestEntities.EntityWithInternal.class.getDeclaredField("internalField");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithInternal.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "secret", entity, document, metadata);

    serializer.serialize(context);

    // @Internal fields are now stored in the document (for primitive types)
    assertTrue(
        "Document should contain @Internal field with primitive value",
        document.containsKey("internalField"));
    assertEquals("secret", document.get("internalField"));
  }

  @Test
  public void testInternalFieldSerializer_DoesNotHandleNormalField() throws Exception {
    final InternalFieldSerializer serializer = new InternalFieldSerializer();
    final TestEntities.SimpleEntity entity = new TestEntities.SimpleEntity();

    final Field field = TestEntities.SimpleEntity.class.getDeclaredField("name");
    final EntityMetadata metadata = metadataExtractor.getMetadata(TestEntities.SimpleEntity.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "test", entity, document, metadata);

    assertFalse("Should not handle normal fields", serializer.canHandle(context));
  }

  @Test
  public void testInternalFieldSerializer_Priority() {
    final InternalFieldSerializer serializer = new InternalFieldSerializer();
    assertEquals("Priority should be 5", 5, serializer.getPriority());
  }

  // ==================== ObjectIdSerializer Tests ====================

  @Test
  public void testObjectIdSerializer_CanHandleObjectIdField() throws Exception {
    final ObjectIdSerializer serializer = new ObjectIdSerializer();
    final TestEntities.EntityWithObjectId entity = new TestEntities.EntityWithObjectId();

    final Field field = TestEntities.EntityWithObjectId.class.getDeclaredField("id");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithObjectId.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "123", entity, document, metadata);

    assertTrue("Should handle @ObjectId fields", serializer.canHandle(context));
  }

  @Test
  public void testObjectIdSerializer_SerializesAsUnderscore() throws Exception {
    final ObjectIdSerializer serializer = new ObjectIdSerializer();
    final TestEntities.EntityWithObjectId entity = new TestEntities.EntityWithObjectId();

    final Field field = TestEntities.EntityWithObjectId.class.getDeclaredField("id");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithObjectId.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "123", entity, document, metadata);

    serializer.serialize(context);

    assertTrue("Document should contain _id", document.containsKey("_id"));
    assertEquals("_id should have correct value", "123", document.get("_id"));
    assertFalse("Document should not contain original field name", document.containsKey("id"));
  }

  @Test
  public void testObjectIdSerializer_Priority() {
    final ObjectIdSerializer serializer = new ObjectIdSerializer();
    assertEquals("Priority should be 10", 10, serializer.getPriority());
  }

  // ==================== EnumSerializer Tests ====================

  @Test
  public void testEnumSerializer_CanHandleEnumField() throws Exception {
    final EnumSerializer serializer = new EnumSerializer();
    final TestEntities.EntityWithEnum entity = new TestEntities.EntityWithEnum();

    final Field field = TestEntities.EntityWithEnum.class.getDeclaredField("status");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithEnum.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(
            field, TestEntities.Status.ACTIVE, entity, document, metadata);

    assertTrue("Should handle enum fields", serializer.canHandle(context));
  }

  @Test
  public void testEnumSerializer_SerializesEnumAsString() throws Exception {
    final EnumSerializer serializer = new EnumSerializer();
    final TestEntities.EntityWithEnum entity = new TestEntities.EntityWithEnum();

    final Field field = TestEntities.EntityWithEnum.class.getDeclaredField("status");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithEnum.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(
            field, TestEntities.Status.ACTIVE, entity, document, metadata);

    serializer.serialize(context);

    assertEquals("Enum should be serialized as String", "ACTIVE", document.get("status"));
  }

  @Test
  public void testEnumSerializer_Priority() {
    final EnumSerializer serializer = new EnumSerializer();
    assertEquals("Priority should be 30", 30, serializer.getPriority());
  }

  // ==================== PrimitiveSerializer Tests ====================

  @Test
  public void testPrimitiveSerializer_CanHandleString() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.SimpleEntity entity = new TestEntities.SimpleEntity();

    final Field field = TestEntities.SimpleEntity.class.getDeclaredField("name");
    final EntityMetadata metadata = metadataExtractor.getMetadata(TestEntities.SimpleEntity.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "test", entity, document, metadata);

    assertTrue("Should handle String", serializer.canHandle(context));
  }

  @Test
  public void testPrimitiveSerializer_CanHandleInteger() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, 25, entity, document, metadata);

    assertTrue("Should handle Integer", serializer.canHandle(context));
  }

  @Test
  public void testPrimitiveSerializer_SerializesDirectly() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.SimpleEntity entity = new TestEntities.SimpleEntity();

    final Field field = TestEntities.SimpleEntity.class.getDeclaredField("name");
    final EntityMetadata metadata = metadataExtractor.getMetadata(TestEntities.SimpleEntity.class);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, "test", entity, document, metadata);

    serializer.serialize(context);

    assertEquals("Value should be serialized directly", "test", document.get("name"));
  }

  @Test
  public void testPrimitiveSerializer_Priority() {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    assertEquals("Priority should be 35", 35, serializer.getPriority());
  }

  // ==================== ListSerializer Tests ====================

  @Test
  public void testListSerializer_CanHandleListField() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<String> items = Arrays.asList("a", "b", "c");
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    assertTrue("Should handle List fields", serializer.canHandle(context));
  }

  @Test
  public void testListSerializer_SerializesStringList() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<String> items = Arrays.asList("a", "b", "c");
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    serializer.serialize(context);

    @SuppressWarnings("unchecked")
    final List<String> result = (List<String>) document.get("items");
    assertNotNull("List should be serialized", result);
    assertEquals("List size should match", 3, result.size());
    assertEquals("First item should match", "a", result.get(0));
  }

  @Test
  public void testListSerializer_SerializesEmptyList() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<String> items = new ArrayList<>();
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    serializer.serialize(context);

    @SuppressWarnings("unchecked")
    final List<String> result = (List<String>) document.get("items");
    assertNotNull("Empty list should be serialized", result);
    assertEquals("List should be empty", 0, result.size());
  }

  @Test
  public void testListSerializer_Priority() {
    final ListSerializer serializer = new ListSerializer();
    assertEquals("Priority should be 20", 20, serializer.getPriority());
  }

  // ==================== NestedObjectSerializer Tests ====================

  @Test
  public void testNestedObjectSerializer_CanHandleNestedObject() throws Exception {
    final NestedObjectSerializer serializer = new NestedObjectSerializer();
    final TestEntities.EntityWithNested entity = new TestEntities.EntityWithNested();

    final Field field = TestEntities.EntityWithNested.class.getDeclaredField("nested");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithNested.class);
    final TestEntities.SimpleEntity nested = new TestEntities.SimpleEntity();
    final FieldSerializationContext context =
        new FieldSerializationContext(field, nested, entity, document, metadata);

    assertTrue("Should handle nested objects", serializer.canHandle(context));
  }

  @Test
  public void testNestedObjectSerializer_SerializesNestedObject() throws Exception {
    final NestedObjectSerializer serializer = new NestedObjectSerializer();
    final TestEntities.EntityWithNested entity = new TestEntities.EntityWithNested();

    final Field field = TestEntities.EntityWithNested.class.getDeclaredField("nested");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithNested.class);
    final TestEntities.SimpleEntity nested = new TestEntities.SimpleEntity();
    nested.setName("nested");
    final FieldSerializationContext context =
        new FieldSerializationContext(field, nested, entity, document, metadata);

    serializer.serialize(context);

    final Object result = document.get("nested");
    assertNotNull("Nested object should be serialized", result);
    assertTrue("Result should be Document", result instanceof Document);
    final Document nestedDoc = (Document) result;
    assertEquals("Nested field should be serialized", "nested", nestedDoc.get("name"));
  }

  @Test
  public void testNestedObjectSerializer_Priority() {
    final NestedObjectSerializer serializer = new NestedObjectSerializer();
    assertEquals("Priority should be 25", 25, serializer.getPriority());
  }

  // ==================== ReferenceSerializer Tests ====================

  @Test
  public void testReferenceSerializer_CanHandleReferenceField() throws Exception {
    final ReferenceSerializer serializer = new ReferenceSerializer();
    final TestEntities.EntityWithReference entity = new TestEntities.EntityWithReference();

    final Field field = TestEntities.EntityWithReference.class.getDeclaredField("reference");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithReference.class);
    final TestEntities.EntityWithObjectId ref = new TestEntities.EntityWithObjectId();
    final FieldSerializationContext context =
        new FieldSerializationContext(field, ref, entity, document, metadata);

    assertTrue("Should handle @Reference fields", serializer.canHandle(context));
  }

  @Test
  public void testReferenceSerializer_ExtractsObjectId() throws Exception {
    final ReferenceSerializer serializer = new ReferenceSerializer();
    final TestEntities.EntityWithReference entity = new TestEntities.EntityWithReference();

    final Field field = TestEntities.EntityWithReference.class.getDeclaredField("reference");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithReference.class);
    final TestEntities.EntityWithObjectId ref = new TestEntities.EntityWithObjectId();
    ref.setId("ref123");
    final FieldSerializationContext context =
        new FieldSerializationContext(field, ref, entity, document, metadata);

    serializer.serialize(context);

    assertEquals("Should store only ObjectId", "ref123", document.get("reference"));
  }

  @Test
  public void testReferenceSerializer_Priority() {
    final ReferenceSerializer serializer = new ReferenceSerializer();
    assertEquals("Priority should be 15", 15, serializer.getPriority());
  }

  // ==================== Additional Edge Case Tests ====================

  @Test
  public void testListSerializer_WithNullElements() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<String> items = Arrays.asList("a", null, "c");
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    serializer.serialize(context);

    @SuppressWarnings("unchecked")
    final List<String> result = (List<String>) document.get("items");
    assertEquals("List size should match", 3, result.size());
    assertEquals("First item should match", "a", result.get(0));
    assertNull("Second item should be null", result.get(1));
    assertEquals("Third item should match", "c", result.get(2));
  }

  @Test
  public void testListSerializer_WithLargeList() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<String> items = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      items.add("item" + i);
    }
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    serializer.serialize(context);

    @SuppressWarnings("unchecked")
    final List<String> result = (List<String>) document.get("items");
    assertEquals("List size should match", 1000, result.size());
    assertEquals("Last item should match", "item999", result.get(999));
  }

  @Test
  public void testListSerializer_WithIntegerList() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    serializer.serialize(context);

    @SuppressWarnings("unchecked")
    final List<Integer> result = (List<Integer>) document.get("items");
    assertEquals("List size should match", 5, result.size());
    assertEquals("First item should match", Integer.valueOf(1), result.get(0));
  }

  @Test
  public void testListSerializer_WithMixedPrimitiveTypes() throws Exception {
    final ListSerializer serializer = new ListSerializer();
    final TestEntities.EntityWithList entity = new TestEntities.EntityWithList();

    final Field field = TestEntities.EntityWithList.class.getDeclaredField("items");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithList.class);
    final List<Object> items = Arrays.asList("string", 42, 3.14, true);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, items, entity, document, metadata);

    serializer.serialize(context);

    @SuppressWarnings("unchecked")
    final List<Object> result = (List<Object>) document.get("items");
    assertEquals("List size should match", 4, result.size());
    assertEquals("String should match", "string", result.get(0));
    assertEquals("Integer should match", 42, result.get(1));
  }

  @Test
  public void testListSerializer_ToString() {
    final ListSerializer serializer = new ListSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("ListSerializer"));
    assertTrue(str.contains("20"));
  }

  @Test
  public void testPrimitiveSerializer_WithByte() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Byte value = Byte.valueOf((byte) 127);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Byte", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Byte should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_WithShort() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Short value = Short.valueOf((short) 32000);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Short", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Short should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_WithLong() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Long value = Long.valueOf(9999999999L);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Long", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Long should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_WithFloat() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Float value = Float.valueOf(3.14f);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Float", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Float should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_WithDouble() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Double value = Double.valueOf(3.14159);
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Double", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Double should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_WithBoolean() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Boolean value = Boolean.TRUE;
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Boolean", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Boolean should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_WithCharacter() throws Exception {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final TestEntities.EntityWithPrimitives entity = new TestEntities.EntityWithPrimitives();

    final Field field = TestEntities.EntityWithPrimitives.class.getDeclaredField("age");
    final EntityMetadata metadata =
        metadataExtractor.getMetadata(TestEntities.EntityWithPrimitives.class);
    final Character value = Character.valueOf('A');
    final FieldSerializationContext context =
        new FieldSerializationContext(field, value, entity, document, metadata);

    assertTrue("Should handle Character", serializer.canHandle(context));
    serializer.serialize(context);
    assertEquals("Character should be serialized", value, document.get("age"));
  }

  @Test
  public void testPrimitiveSerializer_ToString() {
    final PrimitiveSerializer serializer = new PrimitiveSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("PrimitiveSerializer"));
  }

  @Test
  public void testEnumSerializer_ToString() {
    final EnumSerializer serializer = new EnumSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("EnumSerializer"));
  }

  @Test
  public void testNestedObjectSerializer_ToString() {
    final NestedObjectSerializer serializer = new NestedObjectSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("NestedObjectSerializer"));
  }

  @Test
  public void testObjectIdSerializer_ToString() {
    final ObjectIdSerializer serializer = new ObjectIdSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("ObjectIdSerializer"));
  }

  @Test
  public void testInternalFieldSerializer_ToString() {
    final InternalFieldSerializer serializer = new InternalFieldSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("InternalFieldSerializer"));
  }

  @Test
  public void testNullValueSerializer_ToString() {
    final NullValueSerializer serializer = new NullValueSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("NullValueSerializer"));
  }

  @Test
  public void testReferenceSerializer_ToString() {
    final ReferenceSerializer serializer = new ReferenceSerializer();
    final String str = serializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("ReferenceSerializer"));
  }
}
