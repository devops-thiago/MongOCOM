package com.arquivolivre.mongocom.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NoSuchMongoCollectionException Tests")
class NoSuchMongoCollectionExceptionTest {

    @Test
    @DisplayName("Should extend Exception")
    void testExtendsException() {
        NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("Test message");
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Should create exception with message")
    void testExceptionWithMessage() {
        String message = "Collection 'users' not found";
        NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with null message")
    void testExceptionWithNullMessage() {
        NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with empty message")
    void testExceptionWithEmptyMessage() {
        String message = "";
        NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().isEmpty());
    }

    @Test
    @DisplayName("Should be throwable")
    void testExceptionIsThrowable() {
        String message = "Test exception";

        Exception thrown = assertThrows(NoSuchMongoCollectionException.class, () -> {
            throw new NoSuchMongoCollectionException(message);
        });

        assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Should maintain stack trace")
    void testStackTrace() {
        NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("Test");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);

        // The first element should be this test method
        assertEquals("testStackTrace", stackTrace[0].getMethodName());
    }

    @Test
    @DisplayName("Should work in try-catch blocks")
    void testTryCatchHandling() {
        String expectedMessage = "Collection not found";
        String actualMessage = null;

        try {
            throw new NoSuchMongoCollectionException(expectedMessage);
        } catch (NoSuchMongoCollectionException e) {
            actualMessage = e.getMessage();
        }

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Should be serializable")
    void testSerializable() {
        NoSuchMongoCollectionException exception = new NoSuchMongoCollectionException("Test");

        // Exception implements Serializable, so this should too
        assertInstanceOf(java.io.Serializable.class, exception);
    }
}



