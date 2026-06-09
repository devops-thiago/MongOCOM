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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.references.ReferenceResolver.ReferenceLoadStrategy;
import com.arquivolivre.mongocom.repository.EntityRepository;
import com.arquivolivre.mongocom.repository.RepositoryFactory;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for ReferenceResolver.
 *
 * @author MongOCOM Team
 */
public class ReferenceResolverTest {

  @Mock private RepositoryFactory mockRepositoryFactory;

  @Mock private EntityRepository<ReferencedEntity, String> mockRepository;

  private ReferenceResolver resolver;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    resolver = new ReferenceResolver(mockRepositoryFactory);
  }

  // ==================== Constructor Tests ====================

  @Test
  public void testDefaultConstructor() {
    final ReferenceResolver defaultResolver = new ReferenceResolver(mockRepositoryFactory);
    assertNotNull(defaultResolver);
    assertEquals(ReferenceLoadStrategy.EAGER, defaultResolver.getLoadStrategy());
  }

  @Test
  public void testConstructorWithStrategy() {
    final ReferenceResolver eagerDirectResolver =
        new ReferenceResolver(mockRepositoryFactory, ReferenceLoadStrategy.EAGER_DIRECT);
    assertEquals(ReferenceLoadStrategy.EAGER_DIRECT, eagerDirectResolver.getLoadStrategy());

    final ReferenceResolver lazyResolver =
        new ReferenceResolver(mockRepositoryFactory, ReferenceLoadStrategy.LAZY);
    assertEquals(ReferenceLoadStrategy.LAZY, lazyResolver.getLoadStrategy());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorWithNullFactory() {
    new ReferenceResolver(null);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorWithNullStrategy() {
    new ReferenceResolver(mockRepositoryFactory, null);
  }

  // ==================== Resolve References Tests ====================

  @Test
  public void testResolveReferencesWithNoReferences() {
    final EntityWithoutReferences entity = new EntityWithoutReferences();
    entity.id = "123";
    entity.name = "Test";

    // Should not throw exception
    resolver.resolveReferences(entity);

    // No repository calls should be made
    verifyNoInteractions(mockRepositoryFactory);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testResolveReferencesWithSingleReference() {
    // Setup mock repository - use any() since field type is Object
    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);

    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";
    referenced.data = "Referenced Data";

    when(mockRepository.findById("ref-123")).thenReturn(Optional.of(referenced));

    // Create entity with reference ID (as Object, simulating deserialized state)
    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = "ref-123"; // ID stored as string initially

    // Resolve references
    resolver.resolveReferences(entity);

    // Verify repository was called (field type is Object, so it gets Object.class)
    verify(mockRepositoryFactory).getRepository(Object.class);
    verify(mockRepository).findById("ref-123");

    // Verify reference was resolved
    assertNotNull(entity.reference);
    assertTrue(entity.reference instanceof ReferencedEntity);
    assertEquals("ref-123", ((ReferencedEntity) entity.reference).id);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testResolveReferencesWithMultipleReferences() {
    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);

    final ReferencedEntity ref1 = new ReferencedEntity();
    ref1.id = "ref-1";

    final ReferencedEntity ref2 = new ReferencedEntity();
    ref2.id = "ref-2";

    when(mockRepository.findById("ref-1")).thenReturn(Optional.of(ref1));
    when(mockRepository.findById("ref-2")).thenReturn(Optional.of(ref2));

    final EntityWithMultipleReferences entity = new EntityWithMultipleReferences();
    entity.id = "main-123";
    entity.reference1 = "ref-1";
    entity.reference2 = "ref-2";

    resolver.resolveReferences(entity);

    verify(mockRepository).findById("ref-1");
    verify(mockRepository).findById("ref-2");
  }

  @Test
  public void testResolveReferencesWithNullReference() {
    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = null;

    // Should not throw exception
    resolver.resolveReferences(entity);

    // No repository calls should be made
    verifyNoInteractions(mockRepositoryFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testResolveReferencesWithNullEntity() {
    resolver.resolveReferences(null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testResolveReferencesWithNotFoundEntity() {
    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);
    when(mockRepository.findById("ref-123")).thenReturn(Optional.empty());

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = "ref-123";

    // Should not throw exception, just log warning
    resolver.resolveReferences(entity);

    verify(mockRepository).findById("ref-123");
    // Reference should remain as string ID
    assertEquals("ref-123", entity.reference);
  }

  // ==================== Caching Tests ====================

  @Test
  @SuppressWarnings("unchecked")
  public void testCachingPreventsRedundantQueries() {
    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);

    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";

    when(mockRepository.findById("ref-123")).thenReturn(Optional.of(referenced));

    // First entity
    final EntityWithReference entity1 = new EntityWithReference();
    entity1.id = "main-1";
    entity1.reference = "ref-123";

    resolver.resolveReferences(entity1);

    // Second entity with same reference
    final EntityWithReference entity2 = new EntityWithReference();
    entity2.id = "main-2";
    entity2.reference = "ref-123";

    resolver.resolveReferences(entity2);

    // Repository should only be called once due to caching
    verify(mockRepository, times(1)).findById("ref-123");
    assertEquals(1, resolver.getCacheSize());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClearResetsCache() {
    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);

    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";

    when(mockRepository.findById("ref-123")).thenReturn(Optional.of(referenced));

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = "ref-123";

    resolver.resolveReferences(entity);
    assertEquals(1, resolver.getCacheSize());

    resolver.clear();
    assertEquals(0, resolver.getCacheSize());

    // After clear, should query again - reset reference to ID
    entity.reference = "ref-123";
    resolver.resolveReferences(entity);
    verify(mockRepository, times(2)).findById("ref-123");
  }

  // ==================== Strategy Tests ====================

  @Test
  @SuppressWarnings("unchecked")
  public void testEagerStrategyLoadsAllReferences() {
    final ReferenceResolver eagerResolver =
        new ReferenceResolver(mockRepositoryFactory, ReferenceLoadStrategy.EAGER);

    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);

    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";

    when(mockRepository.findById("ref-123")).thenReturn(Optional.of(referenced));

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = "ref-123";

    eagerResolver.resolveReferences(entity);

    verify(mockRepository).findById("ref-123");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testEagerDirectStrategyLoadsOnlyDirectReferences() {
    final ReferenceResolver eagerDirectResolver =
        new ReferenceResolver(mockRepositoryFactory, ReferenceLoadStrategy.EAGER_DIRECT);

    when(mockRepositoryFactory.getRepository(any(Class.class))).thenReturn(mockRepository);

    final ReferencedEntity referenced = new ReferencedEntity();
    referenced.id = "ref-123";

    when(mockRepository.findById("ref-123")).thenReturn(Optional.of(referenced));

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = "ref-123";

    eagerDirectResolver.resolveReferences(entity);

    verify(mockRepository).findById("ref-123");
  }

  @Test
  public void testLazyStrategyDoesNotLoadReferences() {
    final ReferenceResolver lazyResolver =
        new ReferenceResolver(mockRepositoryFactory, ReferenceLoadStrategy.LAZY);

    final EntityWithReference entity = new EntityWithReference();
    entity.id = "main-123";
    entity.reference = "ref-123";

    lazyResolver.resolveReferences(entity);

    // No repository calls should be made with LAZY strategy
    verifyNoInteractions(mockRepositoryFactory);
    verifyNoInteractions(mockRepository);
  }

  // ==================== Circular Reference Tests ====================

  @Test
  @SuppressWarnings("unchecked")
  public void testCircularReferenceDetection() {
    // Create separate mock for circular entities
    final EntityRepository<CircularEntity, String> mockCircularRepository =
        (EntityRepository<CircularEntity, String>) mock(EntityRepository.class);
    when(mockRepositoryFactory.getRepository(Object.class))
        .thenReturn((EntityRepository) mockCircularRepository);

    // Create circular reference scenario
    final CircularEntity entity1 = new CircularEntity();
    entity1.id = "entity-1";
    entity1.reference = "entity-2"; // Reference to entity2

    final CircularEntity entity2 = new CircularEntity();
    entity2.id = "entity-2";
    entity2.reference = "entity-1"; // Reference back to entity1

    // Mock repository to return entity2 when entity1 is resolved
    when(mockCircularRepository.findById("entity-2")).thenReturn(Optional.of(entity2));
    when(mockCircularRepository.findById("entity-1")).thenReturn(Optional.of(entity1));

    // Should detect circular reference and not loop infinitely
    resolver.resolveReferences(entity1);

    // Should have attempted to resolve both, but stopped at circular reference
    verify(mockCircularRepository, atLeastOnce()).findById(any());
  }

  // ==================== Utility Method Tests ====================

  @Test
  public void testGetCacheSize() {
    assertEquals(0, resolver.getCacheSize());
  }

  @Test
  public void testGetLoadStrategy() {
    assertEquals(ReferenceLoadStrategy.EAGER, resolver.getLoadStrategy());

    final ReferenceResolver lazyResolver =
        new ReferenceResolver(mockRepositoryFactory, ReferenceLoadStrategy.LAZY);
    assertEquals(ReferenceLoadStrategy.LAZY, lazyResolver.getLoadStrategy());
  }

  @Test
  public void testToString() {
    final String str = resolver.toString();
    assertNotNull(str);
    assertTrue(str.contains("ReferenceResolver"));
    assertTrue(str.contains("strategy="));
    assertTrue(str.contains("cachedEntities="));
    assertTrue(str.contains("resolving="));
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
    @Reference Object reference; // Using Object to allow both String ID and resolved entity
  }

  @Document(collection = "entities_with_multiple_references")
  static class EntityWithMultipleReferences {
    @Id String id;
    @Reference Object reference1;
    @Reference Object reference2;
  }

  @Document(collection = "circular_entities")
  static class CircularEntity {
    @Id String id;
    @Reference Object reference;
  }

  @Document(collection = "typed_circular_entities")
  static class TypedCircularEntity {
    @Id String id;

    @Reference
    TypedCircularEntity reference; // Typed circular reference - will be set via reflection
  }
}
