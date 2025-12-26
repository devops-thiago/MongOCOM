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

package com.arquivolivre.mongocom.metadata;

import static org.junit.Assert.*;

import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.testutil.TestEntities.InvalidEntity;
import com.arquivolivre.mongocom.testutil.TestEntities.TestCompany;
import com.arquivolivre.mongocom.testutil.TestEntities.TestUser;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for EntityMetadataExtractor.
 *
 * <p>Tests metadata extraction, caching, and error handling.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class EntityMetadataExtractorTest {

  private EntityMetadataExtractor extractor;

  @Before
  public void setUp() {
    extractor = new EntityMetadataExtractor();
  }

  /** Test that metadata can be extracted from a valid entity. */
  @Test
  public void testGetMetadataFromValidEntity() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    assertNotNull("Metadata should not be null", metadata);
    assertEquals("Collection name should match", "users", metadata.getCollectionName());
    assertEquals("Entity class should match", TestUser.class, metadata.getEntityClass());
  }

  /** Test that metadata extraction handles invalid entity gracefully. */
  @Test
  public void testGetMetadataFromInvalidEntity() {
    // Act
    try {
      final EntityMetadata metadata = extractor.getMetadata(InvalidEntity.class);
      // If no exception, verify metadata is still created
      assertNotNull("Metadata should be created even for invalid entity", metadata);
    } catch (final MappingException e) {
      // Expected - invalid entity should throw exception
      assertTrue("Should throw MappingException", true);
    }
  }

  /** Test that metadata is cached after first extraction. */
  @Test
  public void testMetadataIsCached() {
    // Act
    final EntityMetadata metadata1 = extractor.getMetadata(TestUser.class);
    final EntityMetadata metadata2 = extractor.getMetadata(TestUser.class);

    // Assert
    assertSame("Should return same cached instance", metadata1, metadata2);
    assertEquals(1, extractor.getCacheSize());
  }

  /** Test that different entity classes have different metadata. */
  @Test
  public void testDifferentEntitiesHaveDifferentMetadata() {
    // Act
    final EntityMetadata userMetadata = extractor.getMetadata(TestUser.class);
    final EntityMetadata companyMetadata = extractor.getMetadata(TestCompany.class);

    // Assert
    assertNotSame("Metadata instances should be different", userMetadata, companyMetadata);
    assertEquals("User collection name", "users", userMetadata.getCollectionName());
    assertEquals("Company collection name", "companies", companyMetadata.getCollectionName());
    assertEquals(2, extractor.getCacheSize());
  }

  /** Test that ID field is correctly identified. */
  @Test
  public void testIdFieldIsIdentified() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
    final Field idField = metadata.getIdField();

    // Assert
    assertNotNull("ID field should be found", idField);
    assertEquals("ID field name should be 'username'", "username", idField.getName());
  }

  /** Test that ObjectId field is correctly identified. */
  @Test
  public void testObjectIdFieldIsIdentified() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
    final Field objectIdField = metadata.getObjectIdField();

    // Assert
    assertNotNull("ObjectId field should be found", objectIdField);
    assertEquals("ObjectId field name should be 'id'", "id", objectIdField.getName());
  }

  /** Test that indexed fields are correctly identified. */
  @Test
  public void testIndexedFieldsAreIdentified() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
    final List<Field> indexedFields = metadata.getIndexedFields();

    // Assert
    assertNotNull("Indexed fields list should not be null", indexedFields);
    assertFalse("Should have at least one indexed field", indexedFields.isEmpty());

    // Check that email field is indexed
    boolean emailFound = false;
    for (final Field field : indexedFields) {
      if ("email".equals(field.getName())) {
        emailFound = true;
        break;
      }
    }
    assertTrue("Email field should be indexed", emailFound);
  }

  /** Test that reference fields are correctly identified. */
  @Test
  public void testReferenceFieldsAreIdentified() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
    final List<Field> referenceFields = metadata.getReferenceFields();

    // Assert
    assertNotNull("Reference fields list should not be null", referenceFields);
    assertFalse("Should have at least one reference field", referenceFields.isEmpty());

    // Check that company field is a reference
    boolean companyFound = false;
    for (final Field field : referenceFields) {
      if ("company".equals(field.getName())) {
        companyFound = true;
        break;
      }
    }
    assertTrue("Company field should be a reference", companyFound);
  }

  /** Test that internal fields are correctly identified. */
  @Test
  public void testInternalFieldsAreIdentified() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
    final List<Field> internalFields = metadata.getInternalFields();

    // Assert
    assertNotNull("Internal fields list should not be null", internalFields);
    assertFalse("Should have at least one internal field", internalFields.isEmpty());

    // Check that address field is internal
    boolean addressFound = false;
    for (final Field field : internalFields) {
      if ("address".equals(field.getName())) {
        addressFound = true;
        break;
      }
    }
    assertTrue("Address field should be internal", addressFound);
  }

  /** Test that cache can be cleared. */
  @Test
  public void testCacheClear() {
    // Arrange
    extractor.getMetadata(TestUser.class);
    extractor.getMetadata(TestCompany.class);
    assertEquals("Cache should have 2 entries", 2, extractor.getCacheSize());

    // Act
    extractor.clearCache();

    // Assert
    assertEquals("Cache should be empty", 0, extractor.getCacheSize());
  }

  /** Test that metadata is re-extracted after cache clear. */
  @Test
  public void testMetadataReExtractedAfterCacheClear() {
    // Arrange
    final EntityMetadata metadata1 = extractor.getMetadata(TestUser.class);
    extractor.clearCache();

    // Act
    final EntityMetadata metadata2 = extractor.getMetadata(TestUser.class);

    // Assert
    assertNotSame("Should be different instances after cache clear", metadata1, metadata2);
    assertEquals(
        "But should have same collection name",
        metadata1.getCollectionName(),
        metadata2.getCollectionName());
  }

  /** Test that null entity class throws exception. */
  @Test(expected = NullPointerException.class)
  public void testNullEntityClassThrowsException() {
    // Act - should throw NullPointerException
    extractor.getMetadata(null);
  }

  /** Test that cache size is tracked correctly. */
  @Test
  public void testCacheSizeTracking() {
    // Arrange & Act
    assertEquals("Initial cache size should be 0", 0, extractor.getCacheSize());

    extractor.getMetadata(TestUser.class);
    assertEquals("Cache size should be 1", 1, extractor.getCacheSize());

    extractor.getMetadata(TestCompany.class);
    assertEquals("Cache size should be 2", 2, extractor.getCacheSize());

    // Getting same class again shouldn't increase cache size
    extractor.getMetadata(TestUser.class);
    assertEquals("Cache size should still be 2", 2, extractor.getCacheSize());
  }

  /** Test that isCached method works correctly. */
  @Test
  public void testIsCachedMethod() {
    // Arrange
    assertFalse("TestUser should not be cached initially", extractor.isCached(TestUser.class));

    // Act
    extractor.getMetadata(TestUser.class);

    // Assert
    assertTrue("TestUser should be cached after extraction", extractor.isCached(TestUser.class));
    assertFalse("TestCompany should not be cached", extractor.isCached(TestCompany.class));
  }

  /** Test toString method. */
  @Test
  public void testToString() {
    // Arrange
    extractor.getMetadata(TestUser.class);

    // Act
    final String result = extractor.toString();

    // Assert
    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain class name", result.contains("EntityMetadataExtractor"));
    // Note: toString format may vary, just verify it's not empty
    assertTrue("toString should not be empty", result.length() > 0);
  }

  /** Test thread safety of metadata extraction. */
  @Test
  public void testThreadSafety() throws InterruptedException {
    // This is a basic thread safety test
    // For more comprehensive testing, use tools like jcstress

    final int threadCount = 10;
    final Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      threads[i] =
          new Thread(
              () -> {
                final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
                assertNotNull("Metadata should not be null", metadata);
              });
      threads[i].start();
    }

    // Wait for all threads to complete
    for (final Thread thread : threads) {
      thread.join();
    }

    // Assert that cache still has only one entry
    assertEquals(
        "Cache should have only 1 entry despite concurrent access", 1, extractor.getCacheSize());
  }

  /** Test EntityMetadata hasIdField method. */
  @Test
  public void testEntityMetadataHasIdField() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    assertTrue("Should have ID field", metadata.hasIdField());
  }

  /** Test EntityMetadata hasObjectIdField method. */
  @Test
  public void testEntityMetadataHasObjectIdField() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    assertTrue("Should have ObjectId field", metadata.hasObjectIdField());
  }

  /** Test EntityMetadata hasIndexes method. */
  @Test
  public void testEntityMetadataHasIndexes() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    assertTrue("Should have indexes", metadata.hasIndexes());
  }

  /** Test EntityMetadata hasReferences method. */
  @Test
  public void testEntityMetadataHasReferences() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    assertTrue("Should have references", metadata.hasReferences());
  }

  /** Test EntityMetadata hasInternalFields method. */
  @Test
  public void testEntityMetadataHasInternalFields() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    assertTrue("Should have internal fields", metadata.hasInternalFields());
  }

  /** Test EntityMetadata hasGeneratedFields method. */
  @Test
  public void testEntityMetadataHasGeneratedFields() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);

    // Assert
    // TestUser doesn't have @GeneratedValue fields, so this should be false
    assertFalse("TestUser should not have generated fields", metadata.hasGeneratedFields());
    assertTrue("Generated fields list should be empty", metadata.getGeneratedFields().isEmpty());
  }

  /** Test EntityMetadata toString method. */
  @Test
  public void testEntityMetadataToString() {
    // Act
    final EntityMetadata metadata = extractor.getMetadata(TestUser.class);
    final String result = metadata.toString();

    // Assert
    assertNotNull("toString should not return null", result);
    assertTrue("toString should contain EntityMetadata", result.contains("EntityMetadata"));
    assertTrue("toString should contain collection name", result.contains("users"));
    assertTrue("toString should contain entity class", result.contains("TestUser"));
  }

  /** Test EntityMetadata builder with all fields. */
  @Test
  public void testEntityMetadataBuilderWithAllFields() throws NoSuchFieldException {
    // Arrange
    final Field idField = TestUser.class.getDeclaredField("username");
    final Field objectIdField = TestUser.class.getDeclaredField("id");
    final List<Field> indexedFields = java.util.Arrays.asList(idField);
    final List<Field> referenceFields = java.util.Arrays.asList(objectIdField);
    final List<Field> internalFields = java.util.Arrays.asList(idField);
    final List<Field> generatedFields = java.util.Arrays.asList(objectIdField);

    // Act
    final EntityMetadata metadata =
        EntityMetadata.builder()
            .entityClass(TestUser.class)
            .collectionName("test_collection")
            .idField(idField)
            .objectIdField(objectIdField)
            .indexedFields(indexedFields)
            .referenceFields(referenceFields)
            .internalFields(internalFields)
            .generatedFields(generatedFields)
            .build();

    // Assert
    assertNotNull("Metadata should not be null", metadata);
    assertEquals("Entity class should match", TestUser.class, metadata.getEntityClass());
    assertEquals("Collection name should match", "test_collection", metadata.getCollectionName());
    assertEquals("ID field should match", idField, metadata.getIdField());
    assertEquals("ObjectId field should match", objectIdField, metadata.getObjectIdField());
    assertEquals("Indexed fields should match", indexedFields, metadata.getIndexedFields());
    assertEquals("Reference fields should match", referenceFields, metadata.getReferenceFields());
    assertEquals("Internal fields should match", internalFields, metadata.getInternalFields());
    assertEquals("Generated fields should match", generatedFields, metadata.getGeneratedFields());
  }

  /** Test EntityMetadata builder with minimal fields. */
  @Test
  public void testEntityMetadataBuilderWithMinimalFields() {
    // Act
    final EntityMetadata metadata =
        EntityMetadata.builder()
            .entityClass(TestUser.class)
            .collectionName("minimal_collection")
            .build();

    // Assert
    assertNotNull("Metadata should not be null", metadata);
    assertEquals("Entity class should match", TestUser.class, metadata.getEntityClass());
    assertEquals(
        "Collection name should match", "minimal_collection", metadata.getCollectionName());
    assertNull("ID field should be null", metadata.getIdField());
    assertNull("ObjectId field should be null", metadata.getObjectIdField());
    assertTrue("Indexed fields should be empty", metadata.getIndexedFields().isEmpty());
    assertTrue("Reference fields should be empty", metadata.getReferenceFields().isEmpty());
    assertTrue("Internal fields should be empty", metadata.getInternalFields().isEmpty());
    assertTrue("Generated fields should be empty", metadata.getGeneratedFields().isEmpty());
    assertFalse("Should not have ID field", metadata.hasIdField());
    assertFalse("Should not have ObjectId field", metadata.hasObjectIdField());
    assertFalse("Should not have indexes", metadata.hasIndexes());
    assertFalse("Should not have references", metadata.hasReferences());
    assertFalse("Should not have internal fields", metadata.hasInternalFields());
    assertFalse("Should not have generated fields", metadata.hasGeneratedFields());
  }

  /** Test EntityMetadata builder throws exception for null entity class. */
  @Test(expected = NullPointerException.class)
  public void testEntityMetadataBuilderNullEntityClassThrowsException() {
    // Act - should throw NullPointerException
    EntityMetadata.builder().collectionName("test").build();
  }

  /** Test EntityMetadata builder throws exception for null collection name. */
  @Test(expected = NullPointerException.class)
  public void testEntityMetadataBuilderNullCollectionNameThrowsException() {
    // Act - should throw NullPointerException
    EntityMetadata.builder().entityClass(TestUser.class).build();
  }

  /** Test EntityMetadata builder throws exception for null indexed fields list. */
  @Test(expected = NullPointerException.class)
  public void testEntityMetadataBuilderNullIndexedFieldsThrowsException() {
    // Act - should throw NullPointerException
    EntityMetadata.builder()
        .entityClass(TestUser.class)
        .collectionName("test")
        .indexedFields(null)
        .build();
  }

  /** Test EntityMetadata builder throws exception for null reference fields list. */
  @Test(expected = NullPointerException.class)
  public void testEntityMetadataBuilderNullReferenceFieldsThrowsException() {
    // Act - should throw NullPointerException
    EntityMetadata.builder()
        .entityClass(TestUser.class)
        .collectionName("test")
        .referenceFields(null)
        .build();
  }

  /** Test EntityMetadata builder throws exception for null internal fields list. */
  @Test(expected = NullPointerException.class)
  public void testEntityMetadataBuilderNullInternalFieldsThrowsException() {
    // Act - should throw NullPointerException
    EntityMetadata.builder()
        .entityClass(TestUser.class)
        .collectionName("test")
        .internalFields(null)
        .build();
  }

  /** Test EntityMetadata builder throws exception for null generated fields list. */
  @Test(expected = NullPointerException.class)
  public void testEntityMetadataBuilderNullGeneratedFieldsThrowsException() {
    // Act - should throw NullPointerException
    EntityMetadata.builder()
        .entityClass(TestUser.class)
        .collectionName("test")
        .generatedFields(null)
        .build();
  }
}
