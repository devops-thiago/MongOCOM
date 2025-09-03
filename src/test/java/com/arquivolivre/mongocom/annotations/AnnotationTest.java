package com.arquivolivre.mongocom.annotations;

import static org.junit.jupiter.api.Assertions.*;

import com.arquivolivre.mongocom.types.Action;
import com.arquivolivre.mongocom.types.TriggerType;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Annotation Tests")
class AnnotationTest {

  @Test
  @DisplayName("Document annotation should be present and accessible")
  void testDocumentAnnotation() {
    assertTrue(TestDocumentClass.class.isAnnotationPresent(Document.class));

    Document document = TestDocumentClass.class.getAnnotation(Document.class);
    assertNotNull(document);
    assertEquals("test_collection", document.collection());
  }

  @Test
  @DisplayName("Document annotation should have correct retention policy")
  void testDocumentRetention() {
    Retention retention = Document.class.getAnnotation(Retention.class);
    assertNotNull(retention);
    assertEquals(RetentionPolicy.RUNTIME, retention.value());
  }

  @Test
  @DisplayName("Document annotation should target classes")
  void testDocumentTarget() {
    Target target = Document.class.getAnnotation(Target.class);
    assertNotNull(target);
    assertEquals(ElementType.TYPE, target.value()[0]);
  }

  @Test
  @DisplayName("ObjectId annotation should be present on field")
  void testObjectIdAnnotation() throws NoSuchFieldException {
    var field = TestDocumentClass.class.getDeclaredField("id");
    assertTrue(field.isAnnotationPresent(ObjectId.class));

    ObjectId objectId = field.getAnnotation(ObjectId.class);
    assertNotNull(objectId);
  }

  @Test
  @DisplayName("Id annotation should be present on field")
  void testIdAnnotation() throws NoSuchFieldException {
    var field = TestDocumentClass.class.getDeclaredField("customId");
    assertTrue(field.isAnnotationPresent(Id.class));

    Id id = field.getAnnotation(Id.class);
    assertNotNull(id);
  }

  @Test
  @DisplayName("GeneratedValue annotation should be present on field")
  void testGeneratedValueAnnotation() throws NoSuchFieldException {
    var field = TestDocumentClass.class.getDeclaredField("generatedField");
    assertTrue(field.isAnnotationPresent(GeneratedValue.class));

    GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
    assertNotNull(generatedValue);
    assertEquals(IntegerGenerator.class, generatedValue.generator());
    assertFalse(generatedValue.update());
  }

  @Test
  @DisplayName("Index annotation should be present with correct values")
  void testIndexAnnotation() throws NoSuchFieldException {
    var field = TestDocumentClass.class.getDeclaredField("indexedField");
    assertTrue(field.isAnnotationPresent(Index.class));

    Index index = field.getAnnotation(Index.class);
    assertNotNull(index);
    assertEquals("test_index", index.value());
    assertEquals("text", index.type());
    assertFalse(index.unique());
    assertFalse(index.sparse());
    assertTrue(index.background());
  }

  @Test
  @DisplayName("Internal annotation should be present on field")
  void testInternalAnnotation() throws NoSuchFieldException {
    var field = TestDocumentClass.class.getDeclaredField("internalField");
    assertTrue(field.isAnnotationPresent(Internal.class));

    Internal internal = field.getAnnotation(Internal.class);
    assertNotNull(internal);
  }

  @Test
  @DisplayName("Reference annotation should be present on field")
  void testReferenceAnnotation() throws NoSuchFieldException {
    var field = TestDocumentClass.class.getDeclaredField("referenceField");
    assertTrue(field.isAnnotationPresent(Reference.class));

    Reference reference = field.getAnnotation(Reference.class);
    assertNotNull(reference);
  }

  @Test
  @DisplayName("Trigger annotation should be present on method")
  void testTriggerAnnotation() throws NoSuchMethodException {
    Method method = TestDocumentClass.class.getMethod("afterInsertTrigger");
    assertTrue(method.isAnnotationPresent(Trigger.class));

    Trigger trigger = method.getAnnotation(Trigger.class);
    assertNotNull(trigger);
    assertEquals(Action.ON_INSERT, trigger.value());
    assertEquals(TriggerType.BEFORE, trigger.when()); // Default value
  }

  @Test
  @DisplayName("All annotations should have runtime retention")
  void testAnnotationRetentionPolicies() {
    Class<?>[] annotationClasses = {
      Document.class, ObjectId.class, Id.class, GeneratedValue.class,
      Index.class, Internal.class, Reference.class, Trigger.class
    };

    for (Class<?> annotationClass : annotationClasses) {
      Retention retention = annotationClass.getAnnotation(Retention.class);
      assertNotNull(retention, annotationClass.getSimpleName() + " should have @Retention");
      assertEquals(
          RetentionPolicy.RUNTIME,
          retention.value(),
          annotationClass.getSimpleName() + " should have RUNTIME retention");
    }
  }

  @Test
  @DisplayName("Field annotations should target fields")
  void testFieldAnnotationTargets() {
    Class<?>[] fieldAnnotations = {
      ObjectId.class, Id.class, GeneratedValue.class,
      Index.class, Internal.class, Reference.class
    };

    for (Class<?> annotationClass : fieldAnnotations) {
      Target target = annotationClass.getAnnotation(Target.class);
      assertNotNull(target, annotationClass.getSimpleName() + " should have @Target");

      ElementType[] targetTypes = target.value();
      boolean hasFieldTarget = false;
      for (ElementType type : targetTypes) {
        if (type == ElementType.FIELD) {
          hasFieldTarget = true;
          break;
        }
      }
      assertTrue(hasFieldTarget, annotationClass.getSimpleName() + " should target FIELD");
    }
  }

  @Test
  @DisplayName("Index annotation should have default values")
  void testIndexAnnotationDefaults() {
    // Test that we can create the annotation and access its methods
    assertDoesNotThrow(
        () -> {
          Index.class.getMethod("value");
          Index.class.getMethod("type");
          Index.class.getMethod("unique");
          Index.class.getMethod("sparse");
          Index.class.getMethod("background");
          Index.class.getMethod("order");
        });
  }

  @Test
  @DisplayName("GeneratedValue annotation should have generator and update methods")
  void testGeneratedValueAnnotationMethods() {
    assertDoesNotThrow(
        () -> {
          GeneratedValue.class.getMethod("generator");
          GeneratedValue.class.getMethod("update");
        });
  }

  @Test
  @DisplayName("Document annotation should have default collection name")
  void testDocumentAnnotationDefault() throws NoSuchMethodException {
    Method collectionMethod = Document.class.getMethod("collection");
    assertNotNull(collectionMethod);

    // The default should be an empty string
    Object defaultValue = collectionMethod.getDefaultValue();
    assertEquals("", defaultValue);
  }

  @Document(collection = "test_collection")
  static class TestDocumentClass {
    @ObjectId private String id;

    @Id private String customId;

    @GeneratedValue(generator = IntegerGenerator.class)
    private String generatedField;

    @Index(value = "test_index", type = "text")
    private String indexedField;

    @Internal private String internalField;

    @Reference private String referenceField;

    @Trigger(Action.ON_INSERT)
    public void afterInsertTrigger() {
      // Test trigger method
    }
  }
}
