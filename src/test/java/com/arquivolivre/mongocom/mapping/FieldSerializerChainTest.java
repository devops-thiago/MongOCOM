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
 * Unit tests for FieldSerializerChain.
 *
 * <p>Tests the Chain of Responsibility pattern implementation for field serialization.
 */
public class FieldSerializerChainTest {

  @Mock private FieldSerializer serializer1;
  @Mock private FieldSerializer serializer2;
  @Mock private FieldSerializer serializer3;
  @Mock private FieldSerializationContext context;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testBuilder_CreateChainWithSingleSerializer() {
    when(serializer1.getPriority()).thenReturn(1);

    FieldSerializerChain chain = FieldSerializerChain.builder().add(serializer1).build();

    assertNotNull("Chain should not be null", chain);
    assertEquals("Chain should have 1 serializer", 1, chain.size());
  }

  @Test
  public void testBuilder_CreateChainWithMultipleSerializers() {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer3.getPriority()).thenReturn(3);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).add(serializer3).build();

    assertNotNull("Chain should not be null", chain);
    assertEquals("Chain should have 3 serializers", 3, chain.size());
  }

  @Test
  public void testBuilder_AddAll() {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);

    List<FieldSerializer> serializers = Arrays.asList(serializer1, serializer2);
    FieldSerializerChain chain = FieldSerializerChain.builder().addAll(serializers).build();

    assertNotNull("Chain should not be null", chain);
    assertEquals("Chain should have 2 serializers", 2, chain.size());
  }

  @Test(expected = NullPointerException.class)
  public void testBuilder_AddNull_ThrowsException() {
    FieldSerializerChain.builder().add(null);
  }

  @Test(expected = NullPointerException.class)
  public void testBuilder_AddAllNull_ThrowsException() {
    FieldSerializerChain.builder().addAll(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilder_BuildWithoutSerializers_ThrowsException() {
    FieldSerializerChain.builder().build();
  }

  @Test
  public void testSerialize_FirstSerializerHandles() throws MappingException {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer1.canHandle(context)).thenReturn(true);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).build();

    chain.serialize(context);

    verify(serializer1).canHandle(context);
    verify(serializer1).serialize(context);
    verify(serializer2, never()).canHandle(context);
    verify(serializer2, never()).serialize(context);
  }

  @Test
  public void testSerialize_SecondSerializerHandles() throws MappingException {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer1.canHandle(context)).thenReturn(false);
    when(serializer2.canHandle(context)).thenReturn(true);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).build();

    chain.serialize(context);

    verify(serializer1).canHandle(context);
    verify(serializer1, never()).serialize(context);
    verify(serializer2).canHandle(context);
    verify(serializer2).serialize(context);
  }

  @Test(expected = MappingException.class)
  public void testSerialize_NoSerializerHandles_ThrowsException() throws MappingException {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer1.canHandle(context)).thenReturn(false);
    when(context.getFieldName()).thenReturn("testField");
    doReturn(String.class).when(context).getFieldType();

    FieldSerializerChain chain = FieldSerializerChain.builder().add(serializer1).build();

    chain.serialize(context);
  }

  @Test
  public void testSerialize_PriorityOrdering() throws MappingException {
    // Add serializers in reverse priority order
    when(serializer1.getPriority()).thenReturn(3);
    when(serializer2.getPriority()).thenReturn(1);
    when(serializer3.getPriority()).thenReturn(2);

    when(serializer2.canHandle(context)).thenReturn(true);

    FieldSerializerChain chain =
        FieldSerializerChain.builder()
            .add(serializer1) // Priority 3
            .add(serializer2) // Priority 1 (should be checked first)
            .add(serializer3) // Priority 2
            .build();

    chain.serialize(context);

    // Serializer2 should be checked first due to priority 1
    verify(serializer2).canHandle(context);
    verify(serializer2).serialize(context);
    verify(serializer1, never()).canHandle(context);
    verify(serializer3, never()).canHandle(context);
  }

  @Test
  public void testGetSerializers_ReturnsUnmodifiableList() {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).build();

    List<FieldSerializer> serializers = chain.getSerializers();

    assertNotNull("Serializers list should not be null", serializers);
    assertEquals("Should have 2 serializers", 2, serializers.size());

    // Try to modify the list - should throw exception
    try {
      serializers.add(serializer3);
      fail("Should not be able to modify serializers list");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  @Test
  public void testGetSerializers_ReturnsSortedByPriority() {
    when(serializer1.getPriority()).thenReturn(3);
    when(serializer2.getPriority()).thenReturn(1);
    when(serializer3.getPriority()).thenReturn(2);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).add(serializer3).build();

    List<FieldSerializer> serializers = chain.getSerializers();

    assertEquals("First should be serializer2 (priority 1)", serializer2, serializers.get(0));
    assertEquals("Second should be serializer3 (priority 2)", serializer3, serializers.get(1));
    assertEquals("Third should be serializer1 (priority 3)", serializer1, serializers.get(2));
  }

  @Test
  public void testSize_ReturnsCorrectCount() {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer3.getPriority()).thenReturn(3);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).add(serializer3).build();

    assertEquals("Size should be 3", 3, chain.size());
  }

  @Test
  public void testBuilder_FluentAPI() {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);

    // Test fluent API chaining
    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).build();

    assertNotNull("Chain should be created", chain);
    assertEquals("Should have 2 serializers", 2, chain.size());
  }

  @Test
  public void testBuilder_MixAddAndAddAll() {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer3.getPriority()).thenReturn(3);

    List<FieldSerializer> list = Arrays.asList(serializer2, serializer3);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).addAll(list).build();

    assertEquals("Should have 3 serializers", 3, chain.size());
  }

  @Test
  public void testSerialize_AllSerializersDecline() throws MappingException {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer1.canHandle(context)).thenReturn(false);
    when(serializer2.canHandle(context)).thenReturn(false);
    when(context.getFieldName()).thenReturn("testField");
    doReturn(String.class).when(context).getFieldType();

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).build();

    try {
      chain.serialize(context);
      fail("Should throw MappingException when no serializer handles the field");
    } catch (MappingException e) {
      assertTrue(
          "Exception message should mention field name", e.getMessage().contains("testField"));
      assertTrue("Exception message should mention field type", e.getMessage().contains("String"));
    }
  }

  @Test
  public void testSerialize_StopsAtFirstMatch() throws MappingException {
    when(serializer1.getPriority()).thenReturn(1);
    when(serializer2.getPriority()).thenReturn(2);
    when(serializer3.getPriority()).thenReturn(3);

    when(serializer1.canHandle(context)).thenReturn(false);
    when(serializer2.canHandle(context)).thenReturn(true);

    FieldSerializerChain chain =
        FieldSerializerChain.builder().add(serializer1).add(serializer2).add(serializer3).build();

    chain.serialize(context);

    verify(serializer1).canHandle(context);
    verify(serializer2).canHandle(context);
    verify(serializer2).serialize(context);
    // serializer3 should never be checked
    verify(serializer3, never()).canHandle(context);
    verify(serializer3, never()).serialize(context);
  }

  @Test
  public void testBuilder_MultipleBuildsFromSameBuilder() {
    when(serializer1.getPriority()).thenReturn(1);

    FieldSerializerChain.Builder builder = FieldSerializerChain.builder().add(serializer1);

    FieldSerializerChain chain1 = builder.build();
    FieldSerializerChain chain2 = builder.build();

    assertNotNull("First chain should be created", chain1);
    assertNotNull("Second chain should be created", chain2);
    assertNotSame("Chains should be different instances", chain1, chain2);
  }
}
