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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for simple deserializers: NullValueDeserializer, EnumDeserializer, and
 * PrimitiveDeserializer.
 */
public class SimpleDeserializersTest {

  @Mock private FieldDeserializationContext context;

  private enum TestEnum {
    VALUE1,
    VALUE2,
    VALUE3
  }

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // ==================== NullValueDeserializer Tests ====================

  @Test
  public void testNullValueDeserializer_CanHandle_NullValue() {
    when(context.isValueNull()).thenReturn(true);

    NullValueDeserializer deserializer = new NullValueDeserializer();

    assertTrue("Should handle null values", deserializer.canHandle(context));
  }

  @Test
  public void testNullValueDeserializer_CannotHandle_NonNullValue() {
    when(context.isValueNull()).thenReturn(false);

    NullValueDeserializer deserializer = new NullValueDeserializer();

    assertFalse("Should not handle non-null values", deserializer.canHandle(context));
  }

  @Test
  public void testNullValueDeserializer_Deserialize_ObjectField() throws Exception {
    when(context.isFieldPrimitive()).thenReturn(false);

    NullValueDeserializer deserializer = new NullValueDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(null);
  }

  @Test(expected = MappingException.class)
  public void testNullValueDeserializer_Deserialize_PrimitiveField_ThrowsException()
      throws Exception {
    when(context.isFieldPrimitive()).thenReturn(true);
    when(context.getFieldName()).thenReturn("testField");
    doReturn(int.class).when(context).getFieldType();

    NullValueDeserializer deserializer = new NullValueDeserializer();
    deserializer.deserialize(context);
  }

  @Test
  public void testNullValueDeserializer_GetPriority() {
    NullValueDeserializer deserializer = new NullValueDeserializer();
    assertEquals("Priority should be 0 (highest)", 0, deserializer.getPriority());
  }

  @Test
  public void testNullValueDeserializer_ToString() {
    NullValueDeserializer deserializer = new NullValueDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("NullValueDeserializer"));
    assertTrue("toString should contain priority", result.contains("0"));
  }

  // ==================== EnumDeserializer Tests ====================

  @Test
  public void testEnumDeserializer_CanHandle_EnumField() {
    when(context.isFieldEnum()).thenReturn(true);

    EnumDeserializer deserializer = new EnumDeserializer();

    assertTrue("Should handle enum fields", deserializer.canHandle(context));
  }

  @Test
  public void testEnumDeserializer_CannotHandle_NonEnumField() {
    when(context.isFieldEnum()).thenReturn(false);

    EnumDeserializer deserializer = new EnumDeserializer();

    assertFalse("Should not handle non-enum fields", deserializer.canHandle(context));
  }

  @Test
  public void testEnumDeserializer_Deserialize_ValidEnumValue() throws Exception {
    when(context.getValue()).thenReturn("VALUE1");
    doReturn(TestEnum.class).when(context).getFieldType();

    EnumDeserializer deserializer = new EnumDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(TestEnum.VALUE1);
  }

  @Test
  public void testEnumDeserializer_Deserialize_AllEnumValues() throws Exception {
    EnumDeserializer deserializer = new EnumDeserializer();

    // Test VALUE1
    when(context.getValue()).thenReturn("VALUE1");
    doReturn(TestEnum.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(TestEnum.VALUE1);

    // Test VALUE2
    reset(context);
    when(context.getValue()).thenReturn("VALUE2");
    doReturn(TestEnum.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(TestEnum.VALUE2);

    // Test VALUE3
    reset(context);
    when(context.getValue()).thenReturn("VALUE3");
    doReturn(TestEnum.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(TestEnum.VALUE3);
  }

  @Test(expected = MappingException.class)
  public void testEnumDeserializer_Deserialize_InvalidEnumValue_ThrowsException() throws Exception {
    when(context.getValue()).thenReturn("INVALID_VALUE");
    when(context.getFieldName()).thenReturn("testField");
    doReturn(TestEnum.class).when(context).getFieldType();

    EnumDeserializer deserializer = new EnumDeserializer();
    deserializer.deserialize(context);
  }

  @Test(expected = MappingException.class)
  public void testEnumDeserializer_Deserialize_NonStringValue_ThrowsException() throws Exception {
    when(context.getValue()).thenReturn(123); // Integer instead of String
    when(context.getFieldName()).thenReturn("testField");

    EnumDeserializer deserializer = new EnumDeserializer();
    deserializer.deserialize(context);
  }

  @Test(expected = MappingException.class)
  public void testEnumDeserializer_Deserialize_NullValue_ThrowsException() throws Exception {
    when(context.getValue()).thenReturn(null);
    when(context.getFieldName()).thenReturn("testField");

    EnumDeserializer deserializer = new EnumDeserializer();
    deserializer.deserialize(context);
  }

  @Test
  public void testEnumDeserializer_GetPriority() {
    EnumDeserializer deserializer = new EnumDeserializer();
    assertEquals("Priority should be 30", 30, deserializer.getPriority());
  }

  @Test
  public void testEnumDeserializer_ToString() {
    EnumDeserializer deserializer = new EnumDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("EnumDeserializer"));
    assertTrue("toString should contain priority", result.contains("30"));
  }

  // ==================== PrimitiveDeserializer Tests ====================

  @Test
  public void testPrimitiveDeserializer_CanHandle_PrimitiveInt() {
    doReturn(int.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    assertTrue("Should handle primitive int", deserializer.canHandle(context));
  }

  @Test
  public void testPrimitiveDeserializer_CanHandle_WrapperInteger() {
    doReturn(Integer.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    assertTrue("Should handle Integer wrapper", deserializer.canHandle(context));
  }

  @Test
  public void testPrimitiveDeserializer_CanHandle_AllPrimitives() {
    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    // Test all primitive types
    doReturn(boolean.class).when(context).getFieldType();
    assertTrue("Should handle boolean", deserializer.canHandle(context));

    doReturn(byte.class).when(context).getFieldType();
    assertTrue("Should handle byte", deserializer.canHandle(context));

    doReturn(short.class).when(context).getFieldType();
    assertTrue("Should handle short", deserializer.canHandle(context));

    doReturn(int.class).when(context).getFieldType();
    assertTrue("Should handle int", deserializer.canHandle(context));

    doReturn(long.class).when(context).getFieldType();
    assertTrue("Should handle long", deserializer.canHandle(context));

    doReturn(float.class).when(context).getFieldType();
    assertTrue("Should handle float", deserializer.canHandle(context));

    doReturn(double.class).when(context).getFieldType();
    assertTrue("Should handle double", deserializer.canHandle(context));

    doReturn(char.class).when(context).getFieldType();
    assertTrue("Should handle char", deserializer.canHandle(context));
  }

  @Test
  public void testPrimitiveDeserializer_CanHandle_AllWrappers() {
    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    // Test all wrapper types
    doReturn(Boolean.class).when(context).getFieldType();
    assertTrue("Should handle Boolean", deserializer.canHandle(context));

    doReturn(Byte.class).when(context).getFieldType();
    assertTrue("Should handle Byte", deserializer.canHandle(context));

    doReturn(Short.class).when(context).getFieldType();
    assertTrue("Should handle Short", deserializer.canHandle(context));

    doReturn(Integer.class).when(context).getFieldType();
    assertTrue("Should handle Integer", deserializer.canHandle(context));

    doReturn(Long.class).when(context).getFieldType();
    assertTrue("Should handle Long", deserializer.canHandle(context));

    doReturn(Float.class).when(context).getFieldType();
    assertTrue("Should handle Float", deserializer.canHandle(context));

    doReturn(Double.class).when(context).getFieldType();
    assertTrue("Should handle Double", deserializer.canHandle(context));

    doReturn(Character.class).when(context).getFieldType();
    assertTrue("Should handle Character", deserializer.canHandle(context));
  }

  @Test
  public void testPrimitiveDeserializer_CannotHandle_String() {
    doReturn(String.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    assertFalse("Should not handle String", deserializer.canHandle(context));
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_IntegerValue() throws Exception {
    when(context.getValue()).thenReturn(42);
    doReturn(Integer.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(42);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_IntToPrimitive() throws Exception {
    when(context.getValue()).thenReturn(42);
    doReturn(int.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(42);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_IntegerToLong() throws Exception {
    when(context.getValue()).thenReturn(42);
    doReturn(Long.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(42L);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_IntegerToDouble() throws Exception {
    when(context.getValue()).thenReturn(42);
    doReturn(Double.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(42.0);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_StringToInteger() throws Exception {
    when(context.getValue()).thenReturn("123");
    doReturn(Integer.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(123);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_StringToBoolean() throws Exception {
    when(context.getValue()).thenReturn("true");
    doReturn(Boolean.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(true);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_StringToCharacter() throws Exception {
    when(context.getValue()).thenReturn("A");
    doReturn(Character.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue('A');
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_NullValue() throws Exception {
    when(context.getValue()).thenReturn(null);
    doReturn(Integer.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(null);
  }

  @Test(expected = MappingException.class)
  public void testPrimitiveDeserializer_Deserialize_InvalidStringToInteger_ThrowsException()
      throws Exception {
    when(context.getValue()).thenReturn("not a number");
    when(context.getFieldName()).thenReturn("testField");
    doReturn(Integer.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);
  }

  @Test(expected = MappingException.class)
  public void testPrimitiveDeserializer_Deserialize_MultiCharStringToCharacter_ThrowsException()
      throws Exception {
    when(context.getValue()).thenReturn("ABC");
    when(context.getFieldName()).thenReturn("testField");
    doReturn(Character.class).when(context).getFieldType();

    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    deserializer.deserialize(context);
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_AllNumericTypes() throws Exception {
    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    // Byte
    reset(context);
    when(context.getValue()).thenReturn(10);
    doReturn(Byte.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue((byte) 10);

    // Short
    reset(context);
    when(context.getValue()).thenReturn(1000);
    doReturn(Short.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue((short) 1000);

    // Float
    reset(context);
    when(context.getValue()).thenReturn(3.14);
    doReturn(Float.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(3.14f);
  }

  @Test
  public void testPrimitiveDeserializer_GetPriority() {
    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    assertEquals("Priority should be 35", 35, deserializer.getPriority());
  }

  @Test
  public void testPrimitiveDeserializer_ToString() {
    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("PrimitiveDeserializer"));
    assertTrue("toString should contain priority", result.contains("35"));
  }

  @Test
  public void testPrimitiveDeserializer_Deserialize_StringToAllNumericTypes() throws Exception {
    PrimitiveDeserializer deserializer = new PrimitiveDeserializer();

    // Byte from String
    reset(context);
    when(context.getValue()).thenReturn("10");
    doReturn(Byte.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue((byte) 10);

    // Short from String
    reset(context);
    when(context.getValue()).thenReturn("1000");
    doReturn(Short.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue((short) 1000);

    // Long from String
    reset(context);
    when(context.getValue()).thenReturn("100000");
    doReturn(Long.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(100000L);

    // Float from String
    reset(context);
    when(context.getValue()).thenReturn("3.14");
    doReturn(Float.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(3.14f);

    // Double from String
    reset(context);
    when(context.getValue()).thenReturn("3.14159");
    doReturn(Double.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue(3.14159);
  }
}
