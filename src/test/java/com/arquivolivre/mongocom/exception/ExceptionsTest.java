package com.arquivolivre.mongocom.exception;

import static org.junit.Assert.*;

import com.arquivolivre.mongocom.exceptions.NoSuchMongoCollectionException;
import org.junit.Test;

/**
 * Unit tests for custom exceptions.
 *
 * <p>Tests exception construction, messages, causes, and inheritance.
 */
public class ExceptionsTest {

  // MappingException tests
  @Test
  public void testMappingExceptionWithMessage() {
    String message = "Mapping failed";
    MappingException exception = new MappingException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void testMappingExceptionWithMessageAndCause() {
    String message = "Mapping failed";
    Throwable cause = new IllegalArgumentException("Invalid argument");
    MappingException exception = new MappingException(message, cause);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertSame(cause, exception.getCause());
  }

  @Test
  public void testMappingExceptionWithCause() {
    Throwable cause = new IllegalArgumentException("Invalid argument");
    MappingException exception = new MappingException(cause);

    assertNotNull(exception);
    assertSame(cause, exception.getCause());
    assertTrue(exception.getMessage().contains("IllegalArgumentException"));
  }

  @Test
  public void testMappingExceptionWithNullMessage() {
    MappingException exception = new MappingException((String) null);
    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  public void testMappingExceptionWithNullCause() {
    MappingException exception = new MappingException((Throwable) null);
    assertNotNull(exception);
    assertNull(exception.getCause());
  }

  @Test
  public void testMappingExceptionWithEmptyMessage() {
    MappingException exception = new MappingException("");
    assertNotNull(exception);
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testMappingExceptionIsRuntimeException() {
    MappingException exception = new MappingException("test");
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  public void testMappingExceptionCanBeThrown() {
    try {
      throw new MappingException("Test exception");
    } catch (MappingException e) {
      assertEquals("Test exception", e.getMessage());
    }
  }

  @Test
  public void testMappingExceptionCanBeCaught() {
    try {
      throw new MappingException("Test");
    } catch (RuntimeException e) {
      assertTrue(e instanceof MappingException);
    }
  }

  @Test
  public void testMappingExceptionStackTrace() {
    MappingException exception = new MappingException("Test");
    StackTraceElement[] stackTrace = exception.getStackTrace();
    assertNotNull(stackTrace);
    assertTrue(stackTrace.length > 0);
  }

  @Test
  public void testMappingExceptionToString() {
    MappingException exception = new MappingException("Test message");
    String str = exception.toString();
    assertNotNull(str);
    assertTrue(str.contains("MappingException"));
    assertTrue(str.contains("Test message"));
  }

  @Test
  public void testMappingExceptionSerialVersionUID() {
    // Verify serialVersionUID is defined (for serialization compatibility)
    try {
      java.lang.reflect.Field field = MappingException.class.getDeclaredField("serialVersionUID");
      field.setAccessible(true);
      assertNotNull(field);
      assertEquals(1L, field.getLong(null));
    } catch (Exception e) {
      fail("serialVersionUID should be defined: " + e.getMessage());
    }
  }

  @Test
  public void testMappingExceptionWithNestedCause() {
    Throwable rootCause = new IllegalStateException("Root cause");
    Throwable intermediateCause = new IllegalArgumentException("Intermediate", rootCause);
    MappingException exception = new MappingException("Top level", intermediateCause);

    assertEquals("Top level", exception.getMessage());
    assertEquals(intermediateCause, exception.getCause());
    assertEquals(rootCause, exception.getCause().getCause());
  }

  // NoSuchMongoCollectionException tests
  @Test
  public void testNoSuchMongoCollectionExceptionWithMessage() {
    String message = "Collection not found";
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void testNoSuchMongoCollectionExceptionWithNullMessage() {
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(null);
    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  public void testNoSuchMongoCollectionExceptionWithEmptyMessage() {
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("");
    assertNotNull(exception);
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testNoSuchMongoCollectionExceptionIsCheckedException() {
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("test");
    assertTrue(exception instanceof Exception);
    // NoSuchMongoCollectionException is a checked exception (not RuntimeException)
    Exception ex = exception;
    assertFalse(ex instanceof RuntimeException);
  }

  @Test
  public void testNoSuchMongoCollectionExceptionCanBeThrown() {
    try {
      throw new NoSuchMongoCollectionException("Test exception");
    } catch (NoSuchMongoCollectionException e) {
      assertEquals("Test exception", e.getMessage());
    }
  }

  @Test
  public void testNoSuchMongoCollectionExceptionMustBeCaught() {
    // This test verifies it's a checked exception by requiring catch
    try {
      throwNoSuchMongoCollectionException();
      fail("Should have thrown exception");
    } catch (NoSuchMongoCollectionException e) {
      assertEquals("Collection not found", e.getMessage());
    }
  }

  private void throwNoSuchMongoCollectionException() throws NoSuchMongoCollectionException {
    throw new NoSuchMongoCollectionException("Collection not found");
  }

  @Test
  public void testNoSuchMongoCollectionExceptionStackTrace() {
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("Test");
    StackTraceElement[] stackTrace = exception.getStackTrace();
    assertNotNull(stackTrace);
    assertTrue(stackTrace.length > 0);
  }

  @Test
  public void testNoSuchMongoCollectionExceptionToString() {
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("Test message");
    String str = exception.toString();
    assertNotNull(str);
    assertTrue(str.contains("NoSuchMongoCollectionException"));
    assertTrue(str.contains("Test message"));
  }

  @Test
  public void testNoSuchMongoCollectionExceptionSerialVersionUID() {
    // Verify serialVersionUID is defined
    try {
      java.lang.reflect.Field field =
          NoSuchMongoCollectionException.class.getDeclaredField("serialVersionUID");
      field.setAccessible(true);
      assertNotNull(field);
      assertEquals(1L, field.getLong(null));
    } catch (Exception e) {
      fail("serialVersionUID should be defined: " + e.getMessage());
    }
  }

  // Comparison tests
  @Test
  public void testMappingExceptionVsNoSuchMongoCollectionException() {
    MappingException mappingEx = new MappingException("Mapping error");
    NoSuchMongoCollectionException collectionEx =
        new NoSuchMongoCollectionException("Collection error");

    // MappingException is unchecked (RuntimeException)
    assertTrue(mappingEx instanceof RuntimeException);

    // NoSuchMongoCollectionException is checked (Exception but not RuntimeException)
    assertTrue(collectionEx instanceof Exception);
    Exception ex = collectionEx;
    assertFalse(ex instanceof RuntimeException);
  }

  @Test
  public void testExceptionHierarchy() {
    MappingException mappingEx = new MappingException("test");
    NoSuchMongoCollectionException collectionEx = new NoSuchMongoCollectionException("test");

    // Both are Throwable
    assertTrue(mappingEx instanceof Throwable);
    assertTrue(collectionEx instanceof Throwable);

    // Both are Exception
    assertTrue(mappingEx instanceof Exception);
    assertTrue(collectionEx instanceof Exception);

    // Only MappingException is RuntimeException
    assertTrue(mappingEx instanceof RuntimeException);
    Exception ex = collectionEx;
    assertFalse(ex instanceof RuntimeException);
  }

  @Test
  public void testMappingExceptionWithLongMessage() {
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("This is a very long error message. ");
    }
    MappingException exception = new MappingException(longMessage.toString());
    assertNotNull(exception.getMessage());
    assertTrue(exception.getMessage().length() > 1000);
  }

  @Test
  public void testNoSuchMongoCollectionExceptionWithLongMessage() {
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("This is a very long error message. ");
    }
    NoSuchMongoCollectionException exception =
        new NoSuchMongoCollectionException(longMessage.toString());
    assertNotNull(exception.getMessage());
    assertTrue(exception.getMessage().length() > 1000);
  }

  @Test
  public void testMappingExceptionWithSpecialCharacters() {
    String message = "Error: 日本語 中文 한국어 العربية עברית";
    MappingException exception = new MappingException(message);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testNoSuchMongoCollectionExceptionWithSpecialCharacters() {
    String message = "Collection: 日本語 中文 한국어 not found";
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(message);
    assertEquals(message, exception.getMessage());
  }
}
