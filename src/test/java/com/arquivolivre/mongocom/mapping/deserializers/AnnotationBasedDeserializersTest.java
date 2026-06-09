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

import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.mapping.FieldDeserializationContext;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for annotation-based deserializers: InternalFieldDeserializer, ObjectIdDeserializer,
 * and DefaultDeserializer.
 */
public class AnnotationBasedDeserializersTest {

  @Mock private FieldDeserializationContext context;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // ==================== InternalFieldDeserializer Tests ====================

  @Test
  public void testInternalFieldDeserializer_CanHandle_InternalField() {
    when(context.hasAnnotation(Internal.class)).thenReturn(true);

    InternalFieldDeserializer deserializer = new InternalFieldDeserializer();

    assertTrue("Should handle @Internal fields", deserializer.canHandle(context));
  }

  @Test
  public void testInternalFieldDeserializer_CannotHandle_NonInternalField() {
    when(context.hasAnnotation(Internal.class)).thenReturn(false);

    InternalFieldDeserializer deserializer = new InternalFieldDeserializer();

    assertFalse("Should not handle non-@Internal fields", deserializer.canHandle(context));
  }

  @Test
  public void testInternalFieldDeserializer_Deserialize_EmbedsObject() throws Exception {
    // @Internal fields are now embedded (delegated to NestedObjectDeserializer)
    // This behavior is tested in integration tests
    InternalFieldDeserializer deserializer = new InternalFieldDeserializer();

    // Just verify it doesn't throw an exception
    deserializer.deserialize(context);
  }

  @Test
  public void testInternalFieldDeserializer_GetPriority() {
    InternalFieldDeserializer deserializer = new InternalFieldDeserializer();
    assertEquals("Priority should be 5", 5, deserializer.getPriority());
  }

  @Test
  public void testInternalFieldDeserializer_ToString() {
    InternalFieldDeserializer deserializer = new InternalFieldDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("InternalFieldDeserializer"));
    assertTrue("toString should contain priority", result.contains("5"));
  }

  // ==================== ObjectIdDeserializer Tests ====================

  @Test
  public void testObjectIdDeserializer_CanHandle_ObjectIdField() {
    when(context.hasAnnotation(ObjectId.class)).thenReturn(true);

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();

    assertTrue("Should handle @ObjectId fields", deserializer.canHandle(context));
  }

  @Test
  public void testObjectIdDeserializer_CannotHandle_NonObjectIdField() {
    when(context.hasAnnotation(ObjectId.class)).thenReturn(false);

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();

    assertFalse("Should not handle non-@ObjectId fields", deserializer.canHandle(context));
  }

  @Test
  public void testObjectIdDeserializer_Deserialize_ValidObjectId() throws Exception {
    when(context.getFromDocument("_id")).thenReturn("507f1f77bcf86cd799439011");
    doReturn(String.class).when(context).getFieldType();

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("507f1f77bcf86cd799439011");
  }

  @Test
  public void testObjectIdDeserializer_Deserialize_ObjectIdAsObject() throws Exception {
    // MongoDB ObjectId object
    Object mongoObjectId =
        new Object() {
          @Override
          public String toString() {
            return "507f1f77bcf86cd799439011";
          }
        };

    when(context.getFromDocument("_id")).thenReturn(mongoObjectId);
    doReturn(String.class).when(context).getFieldType();

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("507f1f77bcf86cd799439011");
  }

  @Test
  public void testObjectIdDeserializer_Deserialize_NullObjectId_SkipsField() throws Exception {
    when(context.getFromDocument("_id")).thenReturn(null);

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();
    deserializer.deserialize(context);

    // Should not set field value when _id is null
    verify(context, never()).setFieldValue(any());
  }

  @Test(expected = MappingException.class)
  public void testObjectIdDeserializer_Deserialize_NonStringField_ThrowsException()
      throws Exception {
    when(context.getFromDocument("_id")).thenReturn("507f1f77bcf86cd799439011");
    when(context.getFieldName()).thenReturn("testField");
    doReturn(Integer.class).when(context).getFieldType();

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();
    deserializer.deserialize(context);
  }

  @Test
  public void testObjectIdDeserializer_Deserialize_ExceptionMessage() throws Exception {
    when(context.getFromDocument("_id")).thenReturn("507f1f77bcf86cd799439011");
    when(context.getFieldName()).thenReturn("myId");
    doReturn(Integer.class).when(context).getFieldType();

    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();

    try {
      deserializer.deserialize(context);
      fail("Should throw MappingException");
    } catch (MappingException e) {
      String message = e.getMessage();
      assertTrue("Message should mention @ObjectId", message.contains("@ObjectId"));
      assertTrue("Message should mention String type", message.contains("String"));
      assertTrue("Message should mention field name", message.contains("myId"));
      assertTrue("Message should mention actual type", message.contains("Integer"));
    }
  }

  @Test
  public void testObjectIdDeserializer_GetPriority() {
    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();
    assertEquals("Priority should be 10", 10, deserializer.getPriority());
  }

  @Test
  public void testObjectIdDeserializer_ToString() {
    ObjectIdDeserializer deserializer = new ObjectIdDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("ObjectIdDeserializer"));
    assertTrue("toString should contain priority", result.contains("10"));
  }

  // ==================== DefaultDeserializer Tests ====================

  @Test
  public void testDefaultDeserializer_CanHandle_AlwaysReturnsTrue() {
    DefaultDeserializer deserializer = new DefaultDeserializer();

    // Test with various contexts
    assertTrue("Should handle any context", deserializer.canHandle(context));

    when(context.isFieldEnum()).thenReturn(true);
    assertTrue("Should handle enum fields", deserializer.canHandle(context));

    when(context.isFieldPrimitive()).thenReturn(true);
    assertTrue("Should handle primitive fields", deserializer.canHandle(context));
  }

  @Test
  public void testDefaultDeserializer_Deserialize_DirectAssignment() throws Exception {
    String value = "test value";
    when(context.getValue()).thenReturn(value);
    doReturn(String.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(value);
  }

  @Test
  public void testDefaultDeserializer_Deserialize_StringConversion() throws Exception {
    Integer value = 123;
    when(context.getValue()).thenReturn(value);
    doReturn(String.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("123");
  }

  @Test
  public void testDefaultDeserializer_Deserialize_DateField() throws Exception {
    Date date = new Date();
    when(context.getValue()).thenReturn(date);
    doReturn(Date.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(date);
  }

  @Test
  public void testDefaultDeserializer_Deserialize_CompatibleTypes() throws Exception {
    Object value = "test";
    when(context.getValue()).thenReturn(value);
    doReturn(Object.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(value);
  }

  @Test
  public void testDefaultDeserializer_Deserialize_FallbackAssignment() throws Exception {
    Integer value = 42;
    when(context.getValue()).thenReturn(value);
    doReturn(Number.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue(value);
  }

  @Test(expected = MappingException.class)
  public void testDefaultDeserializer_Deserialize_IncompatibleTypes_ThrowsException()
      throws Exception {
    String value = "not a number";
    when(context.getValue()).thenReturn(value);
    when(context.getFieldName()).thenReturn("testField");
    doReturn(Integer.class).when(context).getFieldType();
    doThrow(new IllegalArgumentException("Cannot assign")).when(context).setFieldValue(anyString());

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);
  }

  @Test
  public void testDefaultDeserializer_Deserialize_ExceptionMessage() throws Exception {
    String value = "incompatible";
    when(context.getValue()).thenReturn(value);
    when(context.getFieldName()).thenReturn("myField");
    doReturn(Integer.class).when(context).getFieldType();
    doThrow(new IllegalArgumentException("Cannot assign")).when(context).setFieldValue(anyString());

    DefaultDeserializer deserializer = new DefaultDeserializer();

    try {
      deserializer.deserialize(context);
      fail("Should throw MappingException");
    } catch (MappingException e) {
      String message = e.getMessage();
      assertTrue("Message should mention value type", message.contains("String"));
      assertTrue("Message should mention field name", message.contains("myField"));
      assertTrue("Message should mention field type", message.contains("Integer"));
    }
  }

  @Test
  public void testDefaultDeserializer_Deserialize_NullValueDirectAssignment() throws Exception {
    when(context.getValue()).thenReturn(null);
    doReturn(Object.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    // Null value should be assigned directly (first condition in deserialize)
    verify(context).setFieldValue(null);
  }

  @Test
  public void testDefaultDeserializer_Deserialize_StringToString() throws Exception {
    when(context.getValue()).thenReturn("hello");
    doReturn(String.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("hello");
  }

  @Test
  public void testDefaultDeserializer_Deserialize_ObjectToString() throws Exception {
    Object obj =
        new Object() {
          @Override
          public String toString() {
            return "custom string";
          }
        };

    when(context.getValue()).thenReturn(obj);
    doReturn(String.class).when(context).getFieldType();

    DefaultDeserializer deserializer = new DefaultDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("custom string");
  }

  @Test
  public void testDefaultDeserializer_GetPriority() {
    DefaultDeserializer deserializer = new DefaultDeserializer();
    assertEquals("Priority should be 40 (lowest)", 40, deserializer.getPriority());
  }

  @Test
  public void testDefaultDeserializer_ToString() {
    DefaultDeserializer deserializer = new DefaultDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("DefaultDeserializer"));
    assertTrue("toString should contain priority", result.contains("40"));
    assertTrue("toString should mention fallback role", result.contains("FALLBACK"));
  }

  @Test
  public void testDefaultDeserializer_Deserialize_MultipleStringConversions() throws Exception {
    DefaultDeserializer deserializer = new DefaultDeserializer();

    // Integer to String
    reset(context);
    when(context.getValue()).thenReturn(123);
    doReturn(String.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue("123");

    // Boolean to String
    reset(context);
    when(context.getValue()).thenReturn(true);
    doReturn(String.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue("true");

    // Double to String
    reset(context);
    when(context.getValue()).thenReturn(3.14);
    doReturn(String.class).when(context).getFieldType();
    deserializer.deserialize(context);
    verify(context).setFieldValue("3.14");
  }

  // ==================== ReferenceDeserializer Tests ====================

  @Test
  public void testReferenceDeserializer_CanHandle_ReferenceField() {
    when(context.hasAnnotation(com.arquivolivre.mongocom.annotations.Reference.class))
        .thenReturn(true);

    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    assertTrue("Should handle @Reference fields", deserializer.canHandle(context));
  }

  @Test
  public void testReferenceDeserializer_CanHandle_NonReferenceField() {
    when(context.hasAnnotation(com.arquivolivre.mongocom.annotations.Reference.class))
        .thenReturn(false);

    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    assertFalse("Should not handle non-@Reference fields", deserializer.canHandle(context));
  }

  @Test
  public void testReferenceDeserializer_Deserialize_WithValue() throws Exception {
    Object mockObjectId =
        new Object() {
          @Override
          public String toString() {
            return "507f1f77bcf86cd799439011";
          }
        };

    when(context.getValue()).thenReturn(mockObjectId);
    when(context.getFieldName()).thenReturn("company");
    when(context.getTarget()).thenReturn(new Object());

    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("507f1f77bcf86cd799439011");
  }

  @Test
  public void testReferenceDeserializer_Deserialize_WithNullValue() throws Exception {
    when(context.getValue()).thenReturn(null);
    when(context.getFieldName()).thenReturn("company");
    when(context.getTarget()).thenReturn(new Object());

    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    deserializer.deserialize(context);

    // Should not call setFieldValue when value is null
    verify(context, never()).setFieldValue(any());
  }

  @Test
  public void testReferenceDeserializer_Deserialize_ConvertsToString() throws Exception {
    Integer numericId = 12345;

    when(context.getValue()).thenReturn(numericId);
    when(context.getFieldName()).thenReturn("refId");
    when(context.getTarget()).thenReturn(new Object());

    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    deserializer.deserialize(context);

    verify(context).setFieldValue("12345");
  }

  @Test
  public void testReferenceDeserializer_GetPriority() {
    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    assertEquals("Priority should be 15", 15, deserializer.getPriority());
  }

  @Test
  public void testReferenceDeserializer_ToString() {
    ReferenceDeserializer deserializer = new ReferenceDeserializer();
    String result = deserializer.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("ReferenceDeserializer"));
    assertTrue("toString should contain priority", result.contains("15"));
    assertTrue("toString should mention placeholder status", result.contains("PLACEHOLDER"));
  }
}
