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

package com.arquivolivre.mongocom.references;

import static org.junit.Assert.*;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.references.ReferenceHandler.ReferenceSaveStrategy;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ReferenceHandler.
 *
 * @author MongOCOM Team
 */
public class ReferenceHandlerTest {

  private ReferenceHandler handler;

  @Before
  public void setUp() {
    handler = new ReferenceHandler();
  }

  // ==================== Constructor Tests ====================

  @Test
  public void testDefaultConstructor() {
    final ReferenceHandler defaultHandler = new ReferenceHandler();
    assertNotNull(defaultHandler);
    assertEquals(ReferenceSaveStrategy.CASCADE_ALL, defaultHandler.getSaveStrategy());
  }

  @Test
  public void testConstructorWithStrategy() {
    final ReferenceHandler cascadeDirectHandler =
        new ReferenceHandler(ReferenceSaveStrategy.CASCADE_DIRECT);
    assertEquals(ReferenceSaveStrategy.CASCADE_DIRECT, cascadeDirectHandler.getSaveStrategy());

    final ReferenceHandler noCascadeHandler =
        new ReferenceHandler(ReferenceSaveStrategy.NO_CASCADE);
    assertEquals(ReferenceSaveStrategy.NO_CASCADE, noCascadeHandler.getSaveStrategy());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorWithNullStrategy() {
    new ReferenceHandler(null);
  }

  // ==================== Process References Tests ====================

  @Test
  public void testProcessReferencesWithNoReferences() {
    final EntityWithoutReferences entity = new EntityWithoutReferences();
    entity.id = "123";
    entity.name = "Test";

    final Set<String> referenceIds = handler.processReferences(entity);

    assertNotNull(referenceIds);
    assertTrue(referenceIds.isEmpty());
  }

  @Test
  public void testProcessReferencesWithSingleReference() {
    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = referenced;

    final Set<String> referenceIds = handler.processReferences(entity);

    assertNotNull(referenceIds);
    assertEquals(1, referenceIds.size());
    assertTrue(referenceIds.contains("ref-123"));
  }

  @Test
  public void testProcessReferencesWithMultipleReferences() {
    final ReferencedEntity ref1 = new ReferencedEntity();
    ref1.id = "ref-1";

    final ReferencedEntity ref2 = new ReferencedEntity();
    ref2.id = "ref-2";

    final EntityWithMultipleReferences entity = new EntityWithMultipleReferences();
    entity.id = "main-123";
    entity.reference1 = ref1;
    entity.reference2 = ref2;

    final Set<String> referenceIds = handler.processReferences(entity);

    assertNotNull(referenceIds);
    assertEquals(2, referenceIds.size());
    assertTrue(referenceIds.contains("ref-1"));
    assertTrue(referenceIds.contains("ref-2"));
  }

  @Test
  public void testProcessReferencesWithNullReference() {
    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = null;

    final Set<String> referenceIds = handler.processReferences(entity);

    assertNotNull(referenceIds);
    assertTrue(referenceIds.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void testProcessReferencesWithNullEntity() {
    handler.processReferences(null);
  }

  @Test(expected = MappingException.class)
  public void testProcessReferencesWithNoIdField() {
    final EntityWithoutId referenced = new EntityWithoutId();

    final EntityWithReferenceToNoId entity = new EntityWithReferenceToNoId();
    entity.id = "main-123";
    entity.reference = referenced;

    handler.processReferences(entity);
  }

  @Test(expected = MappingException.class)
  public void testProcessReferencesWithNullId() {
    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = null; // Null ID

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = referenced;

    handler.processReferences(entity);
  }

  // ==================== Circular Reference Tests ====================

  @Test
  public void testCircularReferenceDetection() {
    final CircularEntity entity1 = new CircularEntity();
    entity1.id = "entity-1";

    final CircularEntity entity2 = new CircularEntity();
    entity2.id = "entity-2";

    // Create circular reference
    entity1.reference = entity2;
    entity2.reference = entity1;

    // Process first entity - should detect circular reference
    final Set<String> referenceIds = handler.processReferences(entity1);

    // Should only get entity2's ID, not loop infinitely
    assertNotNull(referenceIds);
    assertEquals(1, referenceIds.size());
    assertTrue(referenceIds.contains("entity-2"));
  }

  // ==================== Strategy Tests ====================

  @Test
  public void testShouldSaveReferenceWithCascadeAll() {
    final ReferenceHandler cascadeAllHandler =
        new ReferenceHandler(ReferenceSaveStrategy.CASCADE_ALL);

    assertTrue(cascadeAllHandler.shouldSaveReference(0));
    assertTrue(cascadeAllHandler.shouldSaveReference(1));
    assertTrue(cascadeAllHandler.shouldSaveReference(5));
    assertTrue(cascadeAllHandler.shouldSaveReference(100));
  }

  @Test
  public void testShouldSaveReferenceWithCascadeDirect() {
    final ReferenceHandler cascadeDirectHandler =
        new ReferenceHandler(ReferenceSaveStrategy.CASCADE_DIRECT);

    assertTrue(cascadeDirectHandler.shouldSaveReference(0));
    assertTrue(cascadeDirectHandler.shouldSaveReference(1));
    assertFalse(cascadeDirectHandler.shouldSaveReference(2));
    assertFalse(cascadeDirectHandler.shouldSaveReference(5));
  }

  @Test
  public void testShouldSaveReferenceWithNoCascade() {
    final ReferenceHandler noCascadeHandler =
        new ReferenceHandler(ReferenceSaveStrategy.NO_CASCADE);

    assertFalse(noCascadeHandler.shouldSaveReference(0));
    assertFalse(noCascadeHandler.shouldSaveReference(1));
    assertFalse(noCascadeHandler.shouldSaveReference(5));
  }

  // ==================== Clear Tests ====================

  @Test
  public void testClear() {
    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = referenced;

    // Process to populate internal tracking
    handler.processReferences(entity);

    // Clear should reset tracking
    handler.clear();

    // Should be able to process same entity again
    final Set<String> referenceIds = handler.processReferences(entity);
    assertNotNull(referenceIds);
    assertEquals(1, referenceIds.size());
  }

  // ==================== ToString Tests ====================

  @Test
  public void testToString() {
    final String str = handler.toString();
    assertNotNull(str);
    assertTrue(str.contains("ReferenceHandler"));
    assertTrue(str.contains("strategy="));
    assertTrue(str.contains("trackedEntities="));
  }

  // ==================== Test Entities ====================

  @Document(collection = "entities_without_references")
  static class EntityWithoutReferences {
    @Id String id;
    String name;
  }

  @Document(collection = "referenced_entities")
  static class ReferencedEntity {
    @Id String id;
    String data;
  }

  @Document(collection = "entities_with_reference")
  static class EntityWithReference {
    @Id String id;
    @Reference ReferencedEntity reference;
  }

  @Document(collection = "entities_with_multiple_references")
  static class EntityWithMultipleReferences {
    @Id String id;
    @Reference ReferencedEntity reference1;
    @Reference ReferencedEntity reference2;
  }

  @Document(collection = "entities_without_id")
  static class EntityWithoutId {
    String data;
  }

  @Document(collection = "entities_with_reference_to_no_id")
  static class EntityWithReferenceToNoId {
    @Id String id;
    @Reference EntityWithoutId reference;
  }

  @Document(collection = "circular_entities")
  static class CircularEntity {
    @Id String id;
    @Reference CircularEntity reference;
  }
}
