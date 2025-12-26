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
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.exception.MappingException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for FieldDeserializerChain.
 *
 * <p>Tests the Chain of Responsibility pattern implementation for field deserialization.
 */
public class FieldDeserializerChainTest {

  @Mock private FieldDeserializer deserializer1;
  @Mock private FieldDeserializer deserializer2;
  @Mock private FieldDeserializer deserializer3;
  @Mock private FieldDeserializationContext context;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testConstructor_CreateChainWithSingleDeserializer() {
    when(deserializer1.getPriority()).thenReturn(1);

    FieldDeserializerChain chain = new FieldDeserializerChain(Arrays.asList(deserializer1));

    assertNotNull("Chain should not be null", chain);
    assertEquals("Chain should have 1 deserializer", 1, chain.size());
  }

  @Test
  public void testConstructor_CreateChainWithMultipleDeserializers() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);
    when(deserializer3.getPriority()).thenReturn(3);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2, deserializer3));

    assertNotNull("Chain should not be null", chain);
    assertEquals("Chain should have 3 deserializers", 3, chain.size());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor_NullList_ThrowsException() {
    new FieldDeserializerChain(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_EmptyList_ThrowsException() {
    new FieldDeserializerChain(Arrays.asList());
  }

  @Test
  public void testDeserialize_FirstDeserializerHandles() throws Exception {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);
    when(deserializer1.canHandle(context)).thenReturn(true);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2));

    chain.deserialize(context);

    verify(deserializer1).canHandle(context);
    verify(deserializer1).deserialize(context);
    verify(deserializer2, never()).canHandle(context);
    verify(deserializer2, never()).deserialize(context);
  }

  @Test
  public void testDeserialize_SecondDeserializerHandles() throws Exception {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);
    when(deserializer1.canHandle(context)).thenReturn(false);
    when(deserializer2.canHandle(context)).thenReturn(true);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2));

    chain.deserialize(context);

    verify(deserializer1).canHandle(context);
    verify(deserializer1, never()).deserialize(context);
    verify(deserializer2).canHandle(context);
    verify(deserializer2).deserialize(context);
  }

  @Test(expected = MappingException.class)
  public void testDeserialize_NoDeserializerHandles_ThrowsException() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer1.canHandle(context)).thenReturn(false);
    when(context.getFieldName()).thenReturn("testField");
    doReturn(String.class).when(context).getFieldType();

    FieldDeserializerChain chain = new FieldDeserializerChain(Arrays.asList(deserializer1));

    chain.deserialize(context);
  }

  @Test
  public void testDeserialize_PriorityOrdering() throws Exception {
    // Add deserializers in reverse priority order
    when(deserializer1.getPriority()).thenReturn(3);
    when(deserializer2.getPriority()).thenReturn(1);
    when(deserializer3.getPriority()).thenReturn(2);

    when(deserializer2.canHandle(context)).thenReturn(true);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(
            Arrays.asList(
                deserializer1, // Priority 3
                deserializer2, // Priority 1 (should be checked first)
                deserializer3)); // Priority 2

    chain.deserialize(context);

    // Deserializer2 should be checked first due to priority 1
    verify(deserializer2).canHandle(context);
    verify(deserializer2).deserialize(context);
    verify(deserializer1, never()).canHandle(context);
    verify(deserializer3, never()).canHandle(context);
  }

  @Test(expected = NullPointerException.class)
  public void testDeserialize_NullContext_ThrowsException() {
    when(deserializer1.getPriority()).thenReturn(1);

    FieldDeserializerChain chain = new FieldDeserializerChain(Arrays.asList(deserializer1));

    chain.deserialize(null);
  }

  @Test
  public void testDeserialize_DeserializerThrowsException_WrapsInMappingException()
      throws Exception {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer1.canHandle(context)).thenReturn(true);
    when(context.getFieldName()).thenReturn("testField");
    doThrow(new RuntimeException("Test exception")).when(deserializer1).deserialize(context);

    FieldDeserializerChain chain = new FieldDeserializerChain(Arrays.asList(deserializer1));

    try {
      chain.deserialize(context);
      fail("Should throw MappingException");
    } catch (MappingException e) {
      assertTrue(
          "Exception message should mention field name", e.getMessage().contains("testField"));
      assertNotNull("Should have cause", e.getCause());
      assertEquals(
          "Cause should be original exception", "Test exception", e.getCause().getMessage());
    }
  }

  @Test
  public void testGetDeserializers_ReturnsUnmodifiableList() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2));

    List<FieldDeserializer> deserializers = chain.getDeserializers();

    assertNotNull("Deserializers list should not be null", deserializers);
    assertEquals("Should have 2 deserializers", 2, deserializers.size());

    // Try to modify the list - should throw exception
    try {
      deserializers.add(deserializer3);
      fail("Should not be able to modify deserializers list");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  @Test
  public void testGetDeserializers_ReturnsSortedByPriority() {
    when(deserializer1.getPriority()).thenReturn(3);
    when(deserializer2.getPriority()).thenReturn(1);
    when(deserializer3.getPriority()).thenReturn(2);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2, deserializer3));

    List<FieldDeserializer> deserializers = chain.getDeserializers();

    assertEquals("First should be deserializer2 (priority 1)", deserializer2, deserializers.get(0));
    assertEquals(
        "Second should be deserializer3 (priority 2)", deserializer3, deserializers.get(1));
    assertEquals("Third should be deserializer1 (priority 3)", deserializer1, deserializers.get(2));
  }

  @Test
  public void testSize_ReturnsCorrectCount() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);
    when(deserializer3.getPriority()).thenReturn(3);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2, deserializer3));

    assertEquals("Size should be 3", 3, chain.size());
  }

  @Test
  public void testDeserialize_AllDeserializersDecline() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);
    when(deserializer1.canHandle(context)).thenReturn(false);
    when(deserializer2.canHandle(context)).thenReturn(false);
    when(context.getFieldName()).thenReturn("testField");
    doReturn(String.class).when(context).getFieldType();

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2));

    try {
      chain.deserialize(context);
      fail("Should throw MappingException when no deserializer handles the field");
    } catch (MappingException e) {
      assertTrue(
          "Exception message should mention field name", e.getMessage().contains("testField"));
      assertTrue("Exception message should mention field type", e.getMessage().contains("String"));
    }
  }

  @Test
  public void testDeserialize_StopsAtFirstMatch() throws Exception {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);
    when(deserializer3.getPriority()).thenReturn(3);

    when(deserializer1.canHandle(context)).thenReturn(false);
    when(deserializer2.canHandle(context)).thenReturn(true);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2, deserializer3));

    chain.deserialize(context);

    verify(deserializer1).canHandle(context);
    verify(deserializer2).canHandle(context);
    verify(deserializer2).deserialize(context);
    // deserializer3 should never be checked
    verify(deserializer3, never()).canHandle(context);
    verify(deserializer3, never()).deserialize(context);
  }

  @Test
  public void testToString_ReturnsCorrectFormat() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);

    FieldDeserializerChain chain =
        new FieldDeserializerChain(Arrays.asList(deserializer1, deserializer2));

    String result = chain.toString();

    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("FieldDeserializerChain"));
    assertTrue("toString should contain deserializer count", result.contains("2"));
  }

  @Test
  public void testDeserialize_MultipleCallsWithSameChain() throws Exception {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer1.canHandle(context)).thenReturn(true);

    FieldDeserializerChain chain = new FieldDeserializerChain(Arrays.asList(deserializer1));

    // Call deserialize multiple times
    chain.deserialize(context);
    chain.deserialize(context);
    chain.deserialize(context);

    // Should be called 3 times
    verify(deserializer1, times(3)).canHandle(context);
    verify(deserializer1, times(3)).deserialize(context);
  }

  @Test
  public void testConstructor_PreservesOriginalList() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer2.getPriority()).thenReturn(2);

    List<FieldDeserializer> originalList = Arrays.asList(deserializer1, deserializer2);
    FieldDeserializerChain chain = new FieldDeserializerChain(originalList);

    // Verify chain was created
    assertEquals("Chain should have 2 deserializers", 2, chain.size());

    // Original list should still be usable (not modified)
    assertEquals("Original list should still have 2 elements", 2, originalList.size());
  }

  @Test
  public void testDeserialize_ExceptionMessageContainsFieldInfo() {
    when(deserializer1.getPriority()).thenReturn(1);
    when(deserializer1.canHandle(context)).thenReturn(false);
    when(context.getFieldName()).thenReturn("myField");
    doReturn(Integer.class).when(context).getFieldType();

    FieldDeserializerChain chain = new FieldDeserializerChain(Arrays.asList(deserializer1));

    try {
      chain.deserialize(context);
      fail("Should throw MappingException");
    } catch (MappingException e) {
      String message = e.getMessage();
      assertTrue("Message should contain field name", message.contains("myField"));
      assertTrue("Message should contain field type", message.contains("Integer"));
    }
  }
}
