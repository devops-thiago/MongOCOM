package com.arquivolivre.mongocom.utils;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IntegerGenerator Tests")
class IntegerGeneratorTest {

    @Mock
    private MongoDatabase mockDatabase;

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private FindIterable<Document> mockFindIterable;

    @Mock
    private MongoCursor<Document> mockCursor;

    private IntegerGenerator integerGenerator;

    @BeforeEach
    void setUp() {
        integerGenerator = new IntegerGenerator();

        // Setup mock behavior for MongoDB interactions - use lenient for unused stubs
        lenient().when(mockDatabase.getCollection("values_String")).thenReturn(mockCollection);
        lenient().when(mockCollection.find()).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.first()).thenReturn(null); // Default: no existing document
    }

    @Test
    @DisplayName("Should implement Generator interface")
    void testImplementsGeneratorInterface() {
        assertInstanceOf(Generator.class, integerGenerator);
    }

    @Test
    @DisplayName("Should generate integer value")
    void testGenerateInteger() {
        // Test the actual method call with mocked database
        Object result = integerGenerator.generateValue(String.class, mockDatabase);
        assertNotNull(result);
        assertInstanceOf(Integer.class, result);
    }

    @Test
    @DisplayName("Should have default constructor")
    void testDefaultConstructor() {
        IntegerGenerator generator = new IntegerGenerator();
        assertNotNull(generator);
    }

    @Test
    @DisplayName("Should generate integer when document exists")
    void testGenerateIntegerWithExistingDocument() {
        // Mock existing document with generatedValue and proper ObjectId
        ObjectId objectId = new ObjectId();
        Document existingDoc = new Document("_id", objectId).append("generatedValue", 5);
        when(mockFindIterable.first()).thenReturn(existingDoc);

        Object result = integerGenerator.generateValue(String.class, mockDatabase);

        assertNotNull(result);
        assertInstanceOf(Integer.class, result);
        assertEquals(6, result); // Should increment existing value

        // Verify MongoDB interactions
        verify(mockDatabase).getCollection("values_String");
        verify(mockCollection).find();
        verify(mockCollection).replaceOne(any(Document.class), any(Document.class));
    }

    @Test
    @DisplayName("Should generate integer when document does not exist")
    void testGenerateIntegerWithNewDocument() {
        // Mock no existing document (first() returns null)
        when(mockFindIterable.first()).thenReturn(null);

        Object result = integerGenerator.generateValue(String.class, mockDatabase);

        assertNotNull(result);
        assertInstanceOf(Integer.class, result);
        assertEquals(1, result); // Should start with 1 for new document

        // Verify MongoDB interactions
        verify(mockDatabase).getCollection("values_String");
        verify(mockCollection).find();
        verify(mockCollection).insertOne(any(Document.class));
    }

    // Note: Full integration tests would require a running MongoDB instance
    // These tests focus on the class structure and basic functionality
    // Integration tests should be in a separate test class with @Testcontainers
}




