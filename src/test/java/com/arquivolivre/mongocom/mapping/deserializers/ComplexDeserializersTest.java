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

package com.arquivolivre.mongocom.mapping.deserializers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ListDeserializer and NestedObjectDeserializer.
 *
 * <p>Tests complex deserialization scenarios including lists and nested objects.
 */
public class ComplexDeserializersTest {

  private ListDeserializer listDeserializer;
  private NestedObjectDeserializer nestedObjectDeserializer;

  // Test enums
  private enum TestStatus {
    ACTIVE,
    INACTIVE
  }

  // Test classes for nested objects
  private static class Address {
    private String street;
    private String city;
    private int zipCode;

    public Address() {}
  }

  private static class Person {
    private String name;
    private int age;
    private Address address;
    private TestStatus status;

    public Person() {}
  }

  private static class Container {
    private List<String> strings;
    private List<Integer> numbers;
    private List<TestStatus> statuses;
    private List<Address> addresses;
    private List list; // Raw type for testing

    public Container() {}
  }

  // Additional test classes for edge cases
  private static class ByteContainer {
    private List<Byte> bytes;

    public ByteContainer() {}
  }

  private static class ShortContainer {
    private List<Short> shorts;

    public ShortContainer() {}
  }

  private static class LongContainer {
    private List<Long> longs;

    public LongContainer() {}
  }

  private static class FloatContainer {
    private List<Float> floats;

    public FloatContainer() {}
  }

  private static class DoubleContainer {
    private List<Double> doubles;

    public DoubleContainer() {}
  }

  private static class ObjectContainer {
    private List<String> strings;

    public ObjectContainer() {}
  }

  private static class BaseEntity {
    private String id;

    public BaseEntity() {}
  }

  private static class ExtendedEntity extends BaseEntity {
    private String name;

    public ExtendedEntity() {}
  }

  private static class EntityContainer {
    private List<ExtendedEntity> entities;

    public EntityContainer() {}
  }

  private static class EntityHolder {
    private ExtendedEntity entity;

    public EntityHolder() {}
  }

  private static class Level3 {
    private String value;

    public Level3() {}
  }

  private static class Level2 {
    private Level3 level3;

    public Level2() {}
  }

  private static class Level1 {
    private Level2 level2;

    public Level1() {}
  }

  private static class RootHolder {
    private Level1 level1;

    public RootHolder() {}
  }

  private static class ComplexEntity {
    private List<String> tags;
    private TestStatus status;
    private int count;

    public ComplexEntity() {}
  }

  private static class ComplexHolder {
    private ComplexEntity entity;

    public ComplexHolder() {}
  }

  @Before
  public void setUp() {
    listDeserializer = new ListDeserializer();
    nestedObjectDeserializer = new NestedObjectDeserializer();
  }

  // ==================== ListDeserializer Tests ====================

  @Test
  public void testListDeserializer_GetPriority() {
    assertEquals(20, listDeserializer.getPriority());
  }

  @Test
  public void testListDeserializer_CanHandle_WithListField() throws Exception {
    final Field field = Container.class.getDeclaredField("strings");
    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    doReturn(field.getType()).when(context).getFieldType();

    assertTrue(listDeserializer.canHandle(context));
  }

  @Test
  public void testListDeserializer_CanHandle_WithNonListField() throws Exception {
    final Field field = Person.class.getDeclaredField("name");
    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    doReturn(field.getType()).when(context).getFieldType();

    assertFalse(listDeserializer.canHandle(context));
  }

  @Test
  public void testListDeserializer_DeserializeStringList() throws Exception {
    final Field field = Container.class.getDeclaredField("strings");
    final Container container = new Container();
    final List<String> sourceList = Arrays.asList("one", "two", "three");

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("strings");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<String> list = (List<String>) value;
                  return list.size() == 3 && list.contains("one") && list.contains("two");
                }));
  }

  @Test
  public void testListDeserializer_DeserializeIntegerList() throws Exception {
    final Field field = Container.class.getDeclaredField("numbers");
    final Container container = new Container();
    final List<Integer> sourceList = Arrays.asList(1, 2, 3, 4, 5);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("numbers");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Integer> list = (List<Integer>) value;
                  return list.size() == 5 && list.get(0) == 1 && list.get(4) == 5;
                }));
  }

  @Test
  public void testListDeserializer_DeserializeEnumList() throws Exception {
    final Field field = Container.class.getDeclaredField("statuses");
    final Container container = new Container();
    final List<String> sourceList = Arrays.asList("ACTIVE", "INACTIVE", "ACTIVE");

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("statuses");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<TestStatus> list = (List<TestStatus>) value;
                  return list.size() == 3
                      && list.get(0) == TestStatus.ACTIVE
                      && list.get(1) == TestStatus.INACTIVE;
                }));
  }

  @Test
  public void testListDeserializer_DeserializeNestedObjectList() throws Exception {
    final Field field = Container.class.getDeclaredField("addresses");
    final Container container = new Container();

    final Document doc1 =
        new Document("street", "Main St").append("city", "NYC").append("zipCode", 10001);
    final Document doc2 =
        new Document("street", "Oak Ave").append("city", "LA").append("zipCode", 90001);
    final List<Document> sourceList = Arrays.asList(doc1, doc2);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("addresses");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Address> list = (List<Address>) value;
                  return list.size() == 2
                      && list.get(0).street.equals("Main St")
                      && list.get(1).city.equals("LA");
                }));
  }

  @Test
  public void testListDeserializer_DeserializeEmptyList() throws Exception {
    final Field field = Container.class.getDeclaredField("strings");
    final Container container = new Container();
    final List<String> sourceList = new ArrayList<>();

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("strings");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<String> list = (List<String>) value;
                  return list.isEmpty();
                }));
  }

  @Test
  public void testListDeserializer_DeserializeListWithNulls() throws Exception {
    final Field field = Container.class.getDeclaredField("strings");
    final Container container = new Container();
    final List<String> sourceList = Arrays.asList("one", null, "three");

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("strings");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<String> list = (List<String>) value;
                  return list.size() == 3 && list.get(1) == null;
                }));
  }

  @Test(expected = MappingException.class)
  public void testListDeserializer_WithNonListValue_ShouldThrowException() throws Exception {
    final Field field = Container.class.getDeclaredField("strings");
    final Container container = new Container();

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("strings");
    when(context.getValue()).thenReturn("not a list");
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);
  }

  @Test(expected = MappingException.class)
  public void testListDeserializer_WithRawType_ShouldThrowException() throws Exception {
    final Field field = Container.class.getDeclaredField("list");
    final Container container = new Container();
    final List<String> sourceList = Arrays.asList("one", "two");

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("list");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);
  }

  @Test
  public void testListDeserializer_ToString() {
    final String str = listDeserializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("ListDeserializer"));
    assertTrue(str.contains("20"));
  }

  // ==================== NestedObjectDeserializer Tests ====================

  @Test
  public void testNestedObjectDeserializer_GetPriority() {
    assertEquals(25, nestedObjectDeserializer.getPriority());
  }

  @Test
  public void testNestedObjectDeserializer_CanHandle_WithDocument() {
    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getValue()).thenReturn(new Document());

    assertTrue(nestedObjectDeserializer.canHandle(context));
  }

  @Test
  public void testNestedObjectDeserializer_CanHandle_WithNonDocument() {
    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getValue()).thenReturn("not a document");

    assertFalse(nestedObjectDeserializer.canHandle(context));
  }

  @Test
  public void testNestedObjectDeserializer_DeserializeSimpleObject() throws Exception {
    final Field field = Person.class.getDeclaredField("address");
    final Person person = new Person();
    final Document doc =
        new Document("street", "Main St").append("city", "NYC").append("zipCode", 10001);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Address.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("address");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(person);

    nestedObjectDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  final Address addr = (Address) value;
                  return addr.street.equals("Main St")
                      && addr.city.equals("NYC")
                      && addr.zipCode == 10001;
                }));
  }

  @Test
  public void testNestedObjectDeserializer_DeserializeObjectWithEnumField() throws Exception {
    final Field field = Person.class.getDeclaredField("address");
    final Person person = new Person();
    // Document with a field that will be converted to enum
    final Document doc =
        new Document("street", "Main St").append("city", "NYC").append("zipCode", 10001);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Address.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("address");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(person);

    nestedObjectDeserializer.deserialize(context);

    verify(context).setFieldValue(argThat(value -> value instanceof Address));
  }

  @Test
  public void testNestedObjectDeserializer_DeserializeWithMissingFields() throws Exception {
    final Field field = Person.class.getDeclaredField("address");
    final Person person = new Person();
    final Document doc = new Document("street", "Main St"); // Missing city and zipCode

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Address.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("address");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(person);

    nestedObjectDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  final Address addr = (Address) value;
                  return addr.street.equals("Main St") && addr.city == null;
                }));
  }

  @Test
  public void testNestedObjectDeserializer_DeserializeWithNullValues() throws Exception {
    final Field field = Person.class.getDeclaredField("address");
    final Person person = new Person();
    final Document doc =
        new Document("street", null).append("city", "NYC").append("zipCode", 10001);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Address.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("address");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(person);

    nestedObjectDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  final Address addr = (Address) value;
                  return addr.street == null && addr.city.equals("NYC");
                }));
  }

  @Test
  public void testNestedObjectDeserializer_DeserializeEmptyDocument() throws Exception {
    final Field field = Person.class.getDeclaredField("address");
    final Person person = new Person();
    final Document doc = new Document();

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Address.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("address");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(person);

    nestedObjectDeserializer.deserialize(context);

    verify(context).setFieldValue(argThat(value -> value instanceof Address));
  }

  @Test(expected = MappingException.class)
  public void testNestedObjectDeserializer_WithNoDefaultConstructor_ShouldThrowException()
      throws Exception {
    // Create a class without default constructor
    class NoDefaultConstructor {
      private final String value;

      public NoDefaultConstructor(String value) {
        this.value = value;
      }
    }

    final Document doc = new Document("value", "test");
    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    doReturn(NoDefaultConstructor.class).when(context).getFieldType();
    when(context.getValue()).thenReturn(doc);

    nestedObjectDeserializer.deserialize(context);
  }

  @Test
  public void testNestedObjectDeserializer_ToString() {
    final String str = nestedObjectDeserializer.toString();
    assertNotNull(str);
    assertTrue(str.contains("NestedObjectDeserializer"));
    assertTrue(str.contains("25"));
  }

  @Test
  public void testNestedObjectDeserializer_WithNumberConversion() throws Exception {
    final Field field = Person.class.getDeclaredField("address");
    final Person person = new Person();
    // MongoDB might return numbers as different types
    final Document doc =
        new Document("street", "Main St")
            .append("city", "NYC")
            .append("zipCode", Integer.valueOf(10001)); // Use Integer

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Address.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("address");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(person);

    nestedObjectDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  final Address addr = (Address) value;
                  return addr.street.equals("Main St")
                      && addr.city.equals("NYC")
                      && addr.zipCode == 10001;
                }));
  }

  // ==================== Additional Edge Case Tests ====================

  @Test
  public void testListDeserializer_NumberConversion_Byte() throws Exception {
    final Field field = ByteContainer.class.getDeclaredField("bytes");
    final ByteContainer container = new ByteContainer();
    final List<Integer> sourceList = Arrays.asList(1, 2, 3);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("bytes");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Byte> list = (List<Byte>) value;
                  return list.size() == 3 && list.get(0) == 1 && list.get(2) == 3;
                }));
  }

  @Test
  public void testListDeserializer_NumberConversion_Short() throws Exception {
    final Field field = ShortContainer.class.getDeclaredField("shorts");
    final ShortContainer container = new ShortContainer();
    final List<Integer> sourceList = Arrays.asList(100, 200, 300);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("shorts");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Short> list = (List<Short>) value;
                  return list.size() == 3 && list.get(0) == 100;
                }));
  }

  @Test
  public void testListDeserializer_NumberConversion_Long() throws Exception {
    final Field field = LongContainer.class.getDeclaredField("longs");
    final LongContainer container = new LongContainer();
    final List<Integer> sourceList = Arrays.asList(1000, 2000, 3000);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("longs");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Long> list = (List<Long>) value;
                  return list.size() == 3 && list.get(0) == 1000L;
                }));
  }

  @Test
  public void testListDeserializer_NumberConversion_Float() throws Exception {
    final Field field = FloatContainer.class.getDeclaredField("floats");
    final FloatContainer container = new FloatContainer();
    final List<Double> sourceList = Arrays.asList(1.5, 2.5, 3.5);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("floats");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Float> list = (List<Float>) value;
                  return list.size() == 3 && Math.abs(list.get(0) - 1.5f) < 0.01;
                }));
  }

  @Test
  public void testListDeserializer_NumberConversion_Double() throws Exception {
    final Field field = DoubleContainer.class.getDeclaredField("doubles");
    final DoubleContainer container = new DoubleContainer();
    final List<Integer> sourceList = Arrays.asList(10, 20, 30);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("doubles");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Double> list = (List<Double>) value;
                  return list.size() == 3 && list.get(0) == 10.0;
                }));
  }

  @Test
  public void testListDeserializer_ToStringConversion() throws Exception {
    final Field field = ObjectContainer.class.getDeclaredField("strings");
    final ObjectContainer container = new ObjectContainer();
    // Use objects that will fall through to toString conversion
    final List<Object> sourceList = Arrays.asList(new Object(), new Object());

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("strings");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<String> list = (List<String>) value;
                  return list.size() == 2 && list.get(0) != null;
                }));
  }

  @Test
  public void testListDeserializer_MixedNumberTypes() throws Exception {
    final Field field = Container.class.getDeclaredField("numbers");
    final Container container = new Container();
    // Mix of Integer, Long, Double
    final List<Number> sourceList = Arrays.asList(1, 2L, 3.0);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("numbers");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Integer> list = (List<Integer>) value;
                  return list.size() == 3 && list.get(0) == 1 && list.get(1) == 2;
                }));
  }

  @Test
  public void testListDeserializer_LargeList() throws Exception {
    final Field field = Container.class.getDeclaredField("numbers");
    final Container container = new Container();
    final List<Integer> sourceList = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      sourceList.add(i);
    }

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("numbers");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<Integer> list = (List<Integer>) value;
                  return list.size() == 1000 && list.get(999) == 999;
                }));
  }

  @Test
  public void testListDeserializer_NestedObjectWithInheritedFields() throws Exception {
    final Field field = EntityContainer.class.getDeclaredField("entities");
    final EntityContainer container = new EntityContainer();

    final Document doc1 = new Document("id", "1").append("name", "Entity1");
    final Document doc2 = new Document("id", "2").append("name", "Entity2");
    final List<Document> sourceList = Arrays.asList(doc1, doc2);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(field.getType()).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("entities");
    when(context.getValue()).thenReturn(sourceList);
    when(context.getTarget()).thenReturn(container);

    listDeserializer.deserialize(context);

    verify(context)
        .setFieldValue(
            argThat(
                value -> {
                  @SuppressWarnings("unchecked")
                  final List<ExtendedEntity> list = (List<ExtendedEntity>) value;
                  return list.size() == 2;
                }));
  }

  @Test
  public void testNestedObjectDeserializer_WithInheritedFields() throws Exception {
    final Field field = EntityHolder.class.getDeclaredField("entity");
    final EntityHolder holder = new EntityHolder();
    final Document doc = new Document("id", "123").append("name", "Test");

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(ExtendedEntity.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("entity");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(holder);

    nestedObjectDeserializer.deserialize(context);

    verify(context).setFieldValue(argThat(value -> value instanceof ExtendedEntity));
  }

  @Test
  public void testNestedObjectDeserializer_WithDeeplyNestedObject() throws Exception {
    final Field field = RootHolder.class.getDeclaredField("level1");
    final RootHolder holder = new RootHolder();

    final Document level3Doc = new Document("value", "deep");
    final Document level2Doc = new Document("level3", level3Doc);
    final Document level1Doc = new Document("level2", level2Doc);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(Level1.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("level1");
    when(context.getValue()).thenReturn(level1Doc);
    when(context.getTarget()).thenReturn(holder);

    nestedObjectDeserializer.deserialize(context);

    verify(context).setFieldValue(argThat(value -> value instanceof Level1));
  }

  @Test
  public void testNestedObjectDeserializer_WithComplexTypes() throws Exception {
    final Field field = ComplexHolder.class.getDeclaredField("entity");
    final ComplexHolder holder = new ComplexHolder();
    final Document doc =
        new Document("tags", Arrays.asList("tag1", "tag2"))
            .append("status", "ACTIVE")
            .append("count", 42);

    final FieldDeserializationContext context = mock(FieldDeserializationContext.class);
    when(context.getField()).thenReturn(field);
    doReturn(ComplexEntity.class).when(context).getFieldType();
    when(context.getFieldName()).thenReturn("entity");
    when(context.getValue()).thenReturn(doc);
    when(context.getTarget()).thenReturn(holder);

    nestedObjectDeserializer.deserialize(context);

    verify(context).setFieldValue(argThat(value -> value instanceof ComplexEntity));
  }
}
