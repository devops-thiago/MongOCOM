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

import com.arquivolivre.mongocom.annotations.*;
import com.arquivolivre.mongocom.testutil.TestEntities.TestUser;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for AnnotationIntrospector utility class.
 *
 * <p>Tests all annotation introspection methods including field and method detection, invocation,
 * and edge cases.
 */
public class AnnotationIntrospectorTest {

  // Test annotation for method invocation tests
  @Retention(RetentionPolicy.RUNTIME)
  private @interface TestAnnotation {}

  // Test class with various annotations
  private static class AnnotatedTestClass {
    @Id private String id;

    @ObjectId private String objectId;

    @Index private String indexedField;

    @Internal private String internalField;

    @Reference private Object reference;

    private String noAnnotation;

    @TestAnnotation
    public void annotatedMethod1() {
      invocationCount++;
    }

    @TestAnnotation
    private void annotatedMethod2() {
      invocationCount++;
    }

    public void notAnnotatedMethod() {
      // Should not be invoked
    }

    private static int invocationCount = 0;

    public static int getInvocationCount() {
      return invocationCount;
    }

    public static void resetInvocationCount() {
      invocationCount = 0;
    }
  }

  // Test class with no annotations
  private static class NoAnnotationsClass {
    private String field1;
    private int field2;

    public void method1() {}

    private void method2() {}
  }

  // Test class with multiple fields having same annotation
  private static class MultipleAnnotationsClass {
    @Index private String field1;

    @Index private String field2;

    @Index private String field3;

    @Reference private Object ref1;

    @Reference private Object ref2;
  }

  @Test
  public void testGetFieldByAnnotation_FindsFirstField() {
    Field field = AnnotationIntrospector.getFieldByAnnotation(AnnotatedTestClass.class, Id.class);

    assertNotNull("Should find @Id field", field);
    assertEquals("Should find 'id' field", "id", field.getName());
  }

  @Test
  public void testGetFieldByAnnotation_ReturnsNullWhenNotFound() {
    Field field = AnnotationIntrospector.getFieldByAnnotation(NoAnnotationsClass.class, Id.class);

    assertNull("Should return null when annotation not found", field);
  }

  @Test
  public void testGetFieldByAnnotation_FindsObjectIdField() {
    Field field =
        AnnotationIntrospector.getFieldByAnnotation(AnnotatedTestClass.class, ObjectId.class);

    assertNotNull("Should find @ObjectId field", field);
    assertEquals("Should find 'objectId' field", "objectId", field.getName());
  }

  @Test
  public void testGetFieldByAnnotation_FindsIndexField() {
    Field field =
        AnnotationIntrospector.getFieldByAnnotation(AnnotatedTestClass.class, Index.class);

    assertNotNull("Should find @Index field", field);
    assertEquals("Should find 'indexedField' field", "indexedField", field.getName());
  }

  @Test
  public void testGetFieldByAnnotation_FindsInternalField() {
    Field field =
        AnnotationIntrospector.getFieldByAnnotation(AnnotatedTestClass.class, Internal.class);

    assertNotNull("Should find @Internal field", field);
    assertEquals("Should find 'internalField' field", "internalField", field.getName());
  }

  @Test
  public void testGetFieldByAnnotation_FindsReferenceField() {
    Field field =
        AnnotationIntrospector.getFieldByAnnotation(AnnotatedTestClass.class, Reference.class);

    assertNotNull("Should find @Reference field", field);
    assertEquals("Should find 'reference' field", "reference", field.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testGetFieldByAnnotation_ThrowsOnNullClass() {
    AnnotationIntrospector.getFieldByAnnotation(null, Id.class);
  }

  @Test(expected = NullPointerException.class)
  public void testGetFieldByAnnotation_ThrowsOnNullAnnotation() {
    AnnotationIntrospector.getFieldByAnnotation(AnnotatedTestClass.class, null);
  }

  @Test
  public void testGetFieldsByAnnotation_FindsAllFields() {
    List<Field> fields =
        AnnotationIntrospector.getFieldsByAnnotation(MultipleAnnotationsClass.class, Index.class);

    assertNotNull("Should return non-null list", fields);
    assertEquals("Should find 3 @Index fields", 3, fields.size());
  }

  @Test
  public void testGetFieldsByAnnotation_ReturnsEmptyListWhenNotFound() {
    List<Field> fields =
        AnnotationIntrospector.getFieldsByAnnotation(NoAnnotationsClass.class, Id.class);

    assertNotNull("Should return non-null list", fields);
    assertTrue("Should return empty list", fields.isEmpty());
  }

  @Test
  public void testGetFieldsByAnnotation_FindsMultipleReferences() {
    List<Field> fields =
        AnnotationIntrospector.getFieldsByAnnotation(
            MultipleAnnotationsClass.class, Reference.class);

    assertNotNull("Should return non-null list", fields);
    assertEquals("Should find 2 @Reference fields", 2, fields.size());
  }

  @Test
  public void testGetFieldsByAnnotation_FindsSingleField() {
    List<Field> fields =
        AnnotationIntrospector.getFieldsByAnnotation(AnnotatedTestClass.class, Id.class);

    assertNotNull("Should return non-null list", fields);
    assertEquals("Should find 1 @Id field", 1, fields.size());
    assertEquals("Should find 'id' field", "id", fields.get(0).getName());
  }

  @Test(expected = NullPointerException.class)
  public void testGetFieldsByAnnotation_ThrowsOnNullClass() {
    AnnotationIntrospector.getFieldsByAnnotation(null, Id.class);
  }

  @Test(expected = NullPointerException.class)
  public void testGetFieldsByAnnotation_ThrowsOnNullAnnotation() {
    AnnotationIntrospector.getFieldsByAnnotation(AnnotatedTestClass.class, null);
  }

  @Test
  public void testGetMethodsByAnnotation_FindsAllMethods() {
    List<Method> methods =
        AnnotationIntrospector.getMethodsByAnnotation(
            AnnotatedTestClass.class, TestAnnotation.class);

    assertNotNull("Should return non-null list", methods);
    assertEquals("Should find 2 @TestAnnotation methods", 2, methods.size());
  }

  @Test
  public void testGetMethodsByAnnotation_ReturnsEmptyListWhenNotFound() {
    List<Method> methods =
        AnnotationIntrospector.getMethodsByAnnotation(NoAnnotationsClass.class, Id.class);

    assertNotNull("Should return non-null list", methods);
    assertTrue("Should return empty list", methods.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void testGetMethodsByAnnotation_ThrowsOnNullClass() {
    AnnotationIntrospector.getMethodsByAnnotation(null, TestAnnotation.class);
  }

  @Test(expected = NullPointerException.class)
  public void testGetMethodsByAnnotation_ThrowsOnNullAnnotation() {
    AnnotationIntrospector.getMethodsByAnnotation(AnnotatedTestClass.class, null);
  }

  @Test
  public void testInvokeAnnotatedMethods_InvokesAllMethods() throws ReflectiveOperationException {
    AnnotatedTestClass.resetInvocationCount();
    AnnotatedTestClass obj = new AnnotatedTestClass();

    AnnotationIntrospector.invokeAnnotatedMethods(obj, TestAnnotation.class);

    assertEquals(
        "Should invoke both annotated methods", 2, AnnotatedTestClass.getInvocationCount());
  }

  @Test
  public void testInvokeAnnotatedMethods_DoesNotInvokeUnannotatedMethods()
      throws ReflectiveOperationException {
    AnnotatedTestClass.resetInvocationCount();
    AnnotatedTestClass obj = new AnnotatedTestClass();

    // Invoke annotated methods
    AnnotationIntrospector.invokeAnnotatedMethods(obj, TestAnnotation.class);

    // Should only invoke the 2 annotated methods, not the unannotated one
    assertEquals(
        "Should only invoke annotated methods", 2, AnnotatedTestClass.getInvocationCount());
  }

  @Test
  public void testInvokeAnnotatedMethods_HandlesNoMethods() throws ReflectiveOperationException {
    NoAnnotationsClass obj = new NoAnnotationsClass();

    // Should not throw exception when no methods found
    AnnotationIntrospector.invokeAnnotatedMethods(obj, TestAnnotation.class);
  }

  @Test(expected = NullPointerException.class)
  public void testInvokeAnnotatedMethods_ThrowsOnNullObject() throws ReflectiveOperationException {
    AnnotationIntrospector.invokeAnnotatedMethods(null, TestAnnotation.class);
  }

  @Test(expected = NullPointerException.class)
  public void testInvokeAnnotatedMethods_ThrowsOnNullAnnotation()
      throws ReflectiveOperationException {
    AnnotatedTestClass obj = new AnnotatedTestClass();
    AnnotationIntrospector.invokeAnnotatedMethods(obj, null);
  }

  @Test
  public void testHasFieldWithAnnotation_ReturnsTrueWhenFound() {
    boolean result =
        AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, Id.class);

    assertTrue("Should return true when @Id field exists", result);
  }

  @Test
  public void testHasFieldWithAnnotation_ReturnsFalseWhenNotFound() {
    boolean result =
        AnnotationIntrospector.hasFieldWithAnnotation(NoAnnotationsClass.class, Id.class);

    assertFalse("Should return false when @Id field does not exist", result);
  }

  @Test
  public void testHasFieldWithAnnotation_ChecksMultipleAnnotations() {
    assertTrue(
        "Should find @Id",
        AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, Id.class));
    assertTrue(
        "Should find @ObjectId",
        AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, ObjectId.class));
    assertTrue(
        "Should find @Index",
        AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, Index.class));
    assertTrue(
        "Should find @Internal",
        AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, Internal.class));
    assertTrue(
        "Should find @Reference",
        AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, Reference.class));
  }

  @Test(expected = NullPointerException.class)
  public void testHasFieldWithAnnotation_ThrowsOnNullClass() {
    AnnotationIntrospector.hasFieldWithAnnotation(null, Id.class);
  }

  @Test(expected = NullPointerException.class)
  public void testHasFieldWithAnnotation_ThrowsOnNullAnnotation() {
    AnnotationIntrospector.hasFieldWithAnnotation(AnnotatedTestClass.class, null);
  }

  @Test
  public void testHasMethodWithAnnotation_ReturnsTrueWhenFound() {
    boolean result =
        AnnotationIntrospector.hasMethodWithAnnotation(
            AnnotatedTestClass.class, TestAnnotation.class);

    assertTrue("Should return true when @TestAnnotation method exists", result);
  }

  @Test
  public void testHasMethodWithAnnotation_ReturnsFalseWhenNotFound() {
    boolean result =
        AnnotationIntrospector.hasMethodWithAnnotation(NoAnnotationsClass.class, Id.class);

    assertFalse("Should return false when annotated method does not exist", result);
  }

  @Test(expected = NullPointerException.class)
  public void testHasMethodWithAnnotation_ThrowsOnNullClass() {
    AnnotationIntrospector.hasMethodWithAnnotation(null, TestAnnotation.class);
  }

  @Test(expected = NullPointerException.class)
  public void testHasMethodWithAnnotation_ThrowsOnNullAnnotation() {
    AnnotationIntrospector.hasMethodWithAnnotation(AnnotatedTestClass.class, null);
  }

  @Test
  public void testCountFieldsWithAnnotation_CountsCorrectly() {
    long count =
        AnnotationIntrospector.countFieldsWithAnnotation(
            MultipleAnnotationsClass.class, Index.class);

    assertEquals("Should count 3 @Index fields", 3, count);
  }

  @Test
  public void testCountFieldsWithAnnotation_ReturnsZeroWhenNotFound() {
    long count =
        AnnotationIntrospector.countFieldsWithAnnotation(NoAnnotationsClass.class, Id.class);

    assertEquals("Should return 0 when no fields found", 0, count);
  }

  @Test
  public void testCountFieldsWithAnnotation_CountsSingleField() {
    long count =
        AnnotationIntrospector.countFieldsWithAnnotation(AnnotatedTestClass.class, Id.class);

    assertEquals("Should count 1 @Id field", 1, count);
  }

  @Test
  public void testCountFieldsWithAnnotation_CountsMultipleReferences() {
    long count =
        AnnotationIntrospector.countFieldsWithAnnotation(
            MultipleAnnotationsClass.class, Reference.class);

    assertEquals("Should count 2 @Reference fields", 2, count);
  }

  @Test(expected = NullPointerException.class)
  public void testCountFieldsWithAnnotation_ThrowsOnNullClass() {
    AnnotationIntrospector.countFieldsWithAnnotation(null, Id.class);
  }

  @Test(expected = NullPointerException.class)
  public void testCountFieldsWithAnnotation_ThrowsOnNullAnnotation() {
    AnnotationIntrospector.countFieldsWithAnnotation(AnnotatedTestClass.class, null);
  }

  @Test
  public void testUtilityClassCannotBeInstantiated() {
    try {
      // Use reflection to try to instantiate
      java.lang.reflect.Constructor<AnnotationIntrospector> constructor =
          AnnotationIntrospector.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Should throw AssertionError when instantiating utility class");
    } catch (Exception e) {
      // Expected - should throw exception
      assertTrue(
          "Should throw AssertionError",
          e.getCause() instanceof AssertionError
              || e.getMessage().contains("Utility class - do not instantiate"));
    }
  }

  @Test
  public void testGetFieldByAnnotation_WorksWithTestUser() {
    Field idField = AnnotationIntrospector.getFieldByAnnotation(TestUser.class, Id.class);
    assertNotNull("Should find @Id field in TestUser", idField);

    Field objectIdField =
        AnnotationIntrospector.getFieldByAnnotation(TestUser.class, ObjectId.class);
    assertNotNull("Should find @ObjectId field in TestUser", objectIdField);
  }

  @Test
  public void testGetFieldsByAnnotation_WorksWithTestUser() {
    List<Field> indexFields =
        AnnotationIntrospector.getFieldsByAnnotation(TestUser.class, Index.class);
    assertNotNull("Should return non-null list", indexFields);
    assertTrue("TestUser should have at least one @Index field", indexFields.size() > 0);
  }

  @Test
  public void testHasFieldWithAnnotation_WorksWithTestUser() {
    assertTrue(
        "TestUser should have @Id field",
        AnnotationIntrospector.hasFieldWithAnnotation(TestUser.class, Id.class));
    assertTrue(
        "TestUser should have @ObjectId field",
        AnnotationIntrospector.hasFieldWithAnnotation(TestUser.class, ObjectId.class));
    assertTrue(
        "TestUser should have @Index field",
        AnnotationIntrospector.hasFieldWithAnnotation(TestUser.class, Index.class));
  }

  @Test
  public void testCountFieldsWithAnnotation_WorksWithTestUser() {
    long idCount = AnnotationIntrospector.countFieldsWithAnnotation(TestUser.class, Id.class);
    assertEquals("TestUser should have exactly 1 @Id field", 1, idCount);

    long objectIdCount =
        AnnotationIntrospector.countFieldsWithAnnotation(TestUser.class, ObjectId.class);
    assertEquals("TestUser should have exactly 1 @ObjectId field", 1, objectIdCount);
  }
}
