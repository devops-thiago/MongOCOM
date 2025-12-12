package com.arquivolivre.mongocom.exceptions;

import static org.junit.Assert.*;
import org.junit.Test;

/** Unit tests for NoSuchMongoCollectionException. */
public class NoSuchMongoCollectionExceptionTest {

  @Test
  public void testExceptionWithMessage() {
    String message = "Collection not found";
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testExceptionIsThrowable() {
    NoSuchMongoCollectionException exception =
        new NoSuchMongoCollectionException("Test exception");

    assertTrue(exception instanceof Exception);
    assertTrue(exception instanceof Throwable);
  }

  @Test
  public void testExceptionWithNullMessage() {
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(null);

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  public void testExceptionWithEmptyMessage() {
    String message = "";
    NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }
}