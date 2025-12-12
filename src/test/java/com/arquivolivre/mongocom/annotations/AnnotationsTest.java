package com.arquivolivre.mongocom.annotations;

import static org.junit.Assert.*;
import org.junit.Test;
import com.arquivolivre.mongocom.types.Action;
import com.arquivolivre.mongocom.types.IndexType;
import com.arquivolivre.mongocom.types.TriggerType;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import com.arquivolivre.mongocom.utils.DateGenerator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Unit tests for all annotations. */
public class AnnotationsTest {

  @Document(collection = "testCollection")
  private static class TestDocumentClass {
    @Id(autoIncrement = true, generator = IntegerGenerator.class)
    private Integer id;

    @Index(value = "nameIndex", unique = true, sparse = false, dropDups = false,
           background = true, order = IndexType.INDEX_ASCENDING, type = "text")
    private String name;

    @GeneratedValue(generator = DateGenerator.class, update = true)
    private java.util.Date createdAt;

    @Reference
    private Object reference;

    @ObjectId
    private String objectId;

    @Internal
    private String internalField;

    @Trigger(value = Action.ON_INSERT, when = TriggerType.BEFORE)
    public void beforeInsert() {}

    @Trigger(value = Action.ON_UPDATE, when = TriggerType.AFTER)
    public void afterUpdate() {}
  }

  @Test
  public void testDocumentAnnotation() throws Exception {
    Document doc = TestDocumentClass.class.getAnnotation(Document.class);
    assertNotNull(doc);
    assertEquals("testCollection", doc.collection());
  }

  @Test
  public void testDocumentAnnotationDefaultValue() {
    @Document
    class DefaultDoc {}

    Document doc = DefaultDoc.class.getAnnotation(Document.class);
    assertNotNull(doc);
    assertEquals("", doc.collection());
  }

  @Test
  public void testIdAnnotation() throws Exception {
    Field field = TestDocumentClass.class.getDeclaredField("id");
    Id id = field.getAnnotation(Id.class);

    assertNotNull(id);
    assertTrue(id.autoIncrement());
    assertEquals(IntegerGenerator.class, id.generator());
  }

  @Test
  public void testIdAnnotationDefaults() throws Exception {
    class TestClass {
      @Id
      private Integer id;
    }

    Field field = TestClass.class.getDeclaredField("id");
    Id id = field.getAnnotation(Id.class);

    assertNotNull(id);
    assertTrue(id.autoIncrement());
    assertEquals(IntegerGenerator.class, id.generator());
  }

  @Test
  public void testIndexAnnotation() throws Exception {
    Field field = TestDocumentClass.class.getDeclaredField("name");
    Index index = field.getAnnotation(Index.class);

    assertNotNull(index);
    assertEquals("nameIndex", index.value());
    assertTrue(index.unique());
    assertFalse(index.sparse());
    assertFalse(index.dropDups());
    assertTrue(index.background());
    assertEquals(IndexType.INDEX_ASCENDING, index.order());
    assertEquals("text", index.type());
  }

  @Test
  public void testIndexAnnotationDefaults() throws Exception {
    class TestClass {
      @Index
      private String field;
    }

    Field field = TestClass.class.getDeclaredField("field");
    Index index = field.getAnnotation(Index.class);

    assertNotNull(index);
    assertEquals("", index.value());
    assertFalse(index.unique());
    assertFalse(index.sparse());
    assertFalse(index.dropDups());
    assertTrue(index.background());
    assertEquals(IndexType.INDEX_ASCENDING, index.order());
    assertEquals("", index.type());
  }

  @Test
  public void testGeneratedValueAnnotation() throws Exception {
    Field field = TestDocumentClass.class.getDeclaredField("createdAt");
    GeneratedValue gen = field.getAnnotation(GeneratedValue.class);

    assertNotNull(gen);
    assertEquals(DateGenerator.class, gen.generator());
    assertTrue(gen.update());
  }

  @Test
  public void testGeneratedValueAnnotationDefaults() throws Exception {
    class TestClass {
      @GeneratedValue(generator = IntegerGenerator.class)
      private Integer field;
    }

    Field field = TestClass.class.getDeclaredField("field");
    GeneratedValue gen = field.getAnnotation(GeneratedValue.class);

    assertNotNull(gen);
    assertEquals(IntegerGenerator.class, gen.generator());
    assertFalse(gen.update());
  }

  @Test
  public void testReferenceAnnotation() throws Exception {
    Field field = TestDocumentClass.class.getDeclaredField("reference");
    Reference ref = field.getAnnotation(Reference.class);

    assertNotNull(ref);
  }

  @Test
  public void testObjectIdAnnotation() throws Exception {
    Field field = TestDocumentClass.class.getDeclaredField("objectId");
    ObjectId oid = field.getAnnotation(ObjectId.class);

    assertNotNull(oid);
  }

  @Test
  public void testInternalAnnotation() throws Exception {
    Field field = TestDocumentClass.class.getDeclaredField("internalField");
    Internal internal = field.getAnnotation(Internal.class);

    assertNotNull(internal);
  }

  @Test
  public void testInternalAnnotationOnClass() {
    @Internal
    class TestClass {}

    Internal internal = TestClass.class.getAnnotation(Internal.class);
    assertNotNull(internal);
  }

  @Test
  public void testTriggerAnnotationOnInsert() throws Exception {
    Method method = TestDocumentClass.class.getDeclaredMethod("beforeInsert");
    Trigger trigger = method.getAnnotation(Trigger.class);

    assertNotNull(trigger);
    assertEquals(Action.ON_INSERT, trigger.value());
    assertEquals(TriggerType.BEFORE, trigger.when());
  }

  @Test
  public void testTriggerAnnotationOnUpdate() throws Exception {
    Method method = TestDocumentClass.class.getDeclaredMethod("afterUpdate");
    Trigger trigger = method.getAnnotation(Trigger.class);

    assertNotNull(trigger);
    assertEquals(Action.ON_UPDATE, trigger.value());
    assertEquals(TriggerType.AFTER, trigger.when());
  }

  @Test
  public void testTriggerAnnotationDefaults() throws Exception {
    class TestClass {
      @Trigger(value = Action.ON_REMOVE)
      public void onRemove() {}
    }

    Method method = TestClass.class.getDeclaredMethod("onRemove");
    Trigger trigger = method.getAnnotation(Trigger.class);

    assertNotNull(trigger);
    assertEquals(Action.ON_REMOVE, trigger.value());
    assertEquals(TriggerType.BEFORE, trigger.when());
  }

  @Test
  public void testMultipleAnnotationsOnSameField() throws Exception {
    class TestClass {
      @Id
      @ObjectId
      private String id;
    }

    Field field = TestClass.class.getDeclaredField("id");
    Id id = field.getAnnotation(Id.class);
    ObjectId oid = field.getAnnotation(ObjectId.class);

    assertNotNull(id);
    assertNotNull(oid);
  }

  @Test
  public void testAnnotationRetention() {
    assertTrue(Document.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(Id.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(Index.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(GeneratedValue.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(Reference.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(ObjectId.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(Internal.class.isAnnotationPresent(java.lang.annotation.Retention.class));
    assertTrue(Trigger.class.isAnnotationPresent(java.lang.annotation.Retention.class));
  }
}