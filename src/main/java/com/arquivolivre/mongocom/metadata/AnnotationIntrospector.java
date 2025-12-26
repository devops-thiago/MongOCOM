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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for annotation-based reflection operations.
 *
 * <p>This class provides efficient annotation introspection using Java Streams API. All methods are
 * static and stateless, making this class thread-safe.
 *
 * <p><b>Thread Safety:</b> This class is thread-safe as all methods are static and stateless. No
 * shared mutable state exists.
 *
 * <p><b>Performance:</b> Methods use Java Streams for efficient filtering and collection
 * operations.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class AnnotationIntrospector {

  /**
   * Private constructor to prevent instantiation.
   *
   * @throws AssertionError if instantiation is attempted
   */
  private AnnotationIntrospector() {
    throw new AssertionError("Utility class - do not instantiate");
  }

  /**
   * Find first field with specified annotation in a class.
   *
   * <p>This method searches through all declared fields (including private fields) of the given
   * class and returns the first field that has the specified annotation.
   *
   * <p><b>Thread Safety:</b> This method is thread-safe as it operates on immutable class metadata.
   *
   * @param clazz the class to search (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @return the first field with the annotation, or null if not found
   * @throws NullPointerException if clazz or annotationClass is null
   */
  public static Field getFieldByAnnotation(
      final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(clazz.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(annotationClass))
        .findFirst()
        .orElse(null);
  }

  /**
   * Find all fields with specified annotation in a class.
   *
   * <p>This method searches through all declared fields (including private fields) of the given
   * class and returns all fields that have the specified annotation.
   *
   * <p><b>Thread Safety:</b> This method is thread-safe. Returns a new list on each invocation.
   *
   * @param clazz the class to search (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @return list of fields with the annotation (never null, may be empty)
   * @throws NullPointerException if clazz or annotationClass is null
   */
  public static List<Field> getFieldsByAnnotation(
      final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(clazz.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(annotationClass))
        .collect(Collectors.toList());
  }

  /**
   * Find all methods with specified annotation in a class.
   *
   * <p>This method searches through all declared methods (including private methods) of the given
   * class and returns all methods that have the specified annotation.
   *
   * <p><b>Thread Safety:</b> This method is thread-safe. Returns a new list on each invocation.
   *
   * @param clazz the class to search (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @return list of methods with the annotation (never null, may be empty)
   * @throws NullPointerException if clazz or annotationClass is null
   */
  public static List<Method> getMethodsByAnnotation(
      final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(clazz.getDeclaredMethods())
        .filter(method -> method.isAnnotationPresent(annotationClass))
        .collect(Collectors.toList());
  }

  /**
   * Invoke all methods with specified annotation on given object.
   *
   * <p>This method finds all methods annotated with the specified annotation and invokes them on
   * the given object. Methods are invoked in the order they are declared in the class.
   *
   * <p><b>Security Note:</b> This method uses reflection to invoke methods, which may increase
   * accessibility of private methods. Ensure only trusted annotations are used.
   *
   * @param obj the object to invoke methods on (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @throws ReflectiveOperationException if method invocation fails
   * @throws NullPointerException if obj or annotationClass is null
   */
  @SuppressWarnings({
    "PMD.AvoidAccessibilityAlteration",
    "spotbugs:REFLC_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_CLASS"
  })
  public static void invokeAnnotatedMethods(
      final Object obj, final Class<? extends Annotation> annotationClass)
      throws ReflectiveOperationException {
    final List<Method> methods = getMethodsByAnnotation(obj.getClass(), annotationClass);

    for (final Method method : methods) {
      method.setAccessible(true);
      method.invoke(obj);
    }
  }

  /**
   * Check if a class has any field with the specified annotation.
   *
   * @param clazz the class to check (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @return true if at least one field has the annotation, false otherwise
   * @throws NullPointerException if clazz or annotationClass is null
   */
  public static boolean hasFieldWithAnnotation(
      final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(clazz.getDeclaredFields())
        .anyMatch(field -> field.isAnnotationPresent(annotationClass));
  }

  /**
   * Check if a class has any method with the specified annotation.
   *
   * @param clazz the class to check (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @return true if at least one method has the annotation, false otherwise
   * @throws NullPointerException if clazz or annotationClass is null
   */
  public static boolean hasMethodWithAnnotation(
      final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(clazz.getDeclaredMethods())
        .anyMatch(method -> method.isAnnotationPresent(annotationClass));
  }

  /**
   * Count fields with specified annotation in a class.
   *
   * @param clazz the class to search (must not be null)
   * @param annotationClass the annotation to find (must not be null)
   * @return number of fields with the annotation
   * @throws NullPointerException if clazz or annotationClass is null
   */
  public static long countFieldsWithAnnotation(
      final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(clazz.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(annotationClass))
        .count();
  }
}
