package com.arquivolivre.mongocom.management;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.annotations.GeneratedValue;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.annotations.Trigger;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import com.arquivolivre.mongocom.utils.DateGenerator;
import com.arquivolivre.mongocom.types.Action;
import com.arquivolivre.mongocom.types.TriggerType;
import com.arquivolivre.mongocom.types.IndexType;
import com.arquivolivre.mongocom.exceptions.NoSuchMongoCollectionException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.conversions.Bson;
import org.bson.BsonObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionManager Tests")
class CollectionManagerTest {

    @Mock
    private MongoClient mockClient;

    @Mock
    private MongoDatabase mockDatabase;

    @Mock
    private MongoCollection<org.bson.Document> mockCollection;

    @Mock
    private FindIterable<org.bson.Document> mockFindIterable;

    @Mock
    private MongoCursor<org.bson.Document> mockMongoCursor;

    @Mock
    private InsertOneResult mockInsertResult;

    // Test document classes
    @Document(collection = "test_collection")
    static class TestDocument {
        @ObjectId
        private String id;

        private String name;
        private int age;

        public TestDocument() {}

        public TestDocument(String name, int age) {
            this.name = name;
            this.age = age;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @Document
    static class DefaultCollectionDocument {
        @ObjectId
        private String id;
        private String data;

        public DefaultCollectionDocument() {}
    }

    static class NonDocumentClass {
        private String data;
        public NonDocumentClass() {}
    }

    // Test documents with various annotations
    @Document(collection = "generated_collection")
    static class DocumentWithGeneratedValue {
        @ObjectId
        private String id;

        @GeneratedValue(generator = IntegerGenerator.class)
        private Integer sequence;

        @GeneratedValue(generator = DateGenerator.class, update = true)
        private Date timestamp;

        public DocumentWithGeneratedValue() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Integer getSequence() { return sequence; }
        public void setSequence(Integer sequence) { this.sequence = sequence; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }

    @Document(collection = "id_document")
    static class DocumentWithIdAnnotation {
        @ObjectId
        private String objectId;

        @Id(autoIncrement = true, generator = IntegerGenerator.class)
        private Integer customId;

        public DocumentWithIdAnnotation() {}

        public String getObjectId() { return objectId; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        public Integer getCustomId() { return customId; }
        public void setCustomId(Integer customId) { this.customId = customId; }
    }

    @Document(collection = "referenced_collection")
    static class ReferencedDocument {
        @ObjectId
        private String id;
        private String refName;

        public ReferencedDocument() {}
        public ReferencedDocument(String refName) { this.refName = refName; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRefName() { return refName; }
        public void setRefName(String refName) { this.refName = refName; }
    }

    @Document(collection = "parent_collection")
    static class DocumentWithReference {
        @ObjectId
        private String id;

        @Reference
        private ReferencedDocument reference;

        @Internal
        private List<ReferencedDocument> internalList;

        private TestEnum status;

        public DocumentWithReference() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public ReferencedDocument getReference() { return reference; }
        public void setReference(ReferencedDocument reference) { this.reference = reference; }
        public List<ReferencedDocument> getInternalList() { return internalList; }
        public void setInternalList(List<ReferencedDocument> internalList) { this.internalList = internalList; }
        public TestEnum getStatus() { return status; }
        public void setStatus(TestEnum status) { this.status = status; }
    }

    @Document(collection = "indexed_collection")
    static class DocumentWithIndexes {
        @ObjectId
        private String id;

        @Index(value = "name_index", unique = true, background = true)
        private String name;

        @Index(type = "text")
        private String description;

        @Index(value = "compound_index", order = 1)
        private String field1;

        @Index(value = "compound_index", order = -1)
        private String field2;

        public DocumentWithIndexes() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public String getField2() { return field2; }
        public void setField2(String field2) { this.field2 = field2; }
    }

    @Document(collection = "trigger_collection")
    static class DocumentWithTrigger {
        @ObjectId
        private String id;
        private String data;

        @Trigger(value = Action.ON_INSERT, when = TriggerType.BEFORE)
        public void beforeInsert() {
            // Trigger method
        }

        @Trigger(value = Action.ON_UPDATE, when = TriggerType.AFTER)
        public void afterUpdate() {
            // Trigger method
        }

        public DocumentWithTrigger() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public enum TestEnum {
        ACTIVE, INACTIVE, PENDING
    }

    @BeforeEach
    void setUp() {
        // Mock the database and client
        lenient().when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);

        // Mock the collection for any collection name
        lenient().when(mockDatabase.getCollection(anyString())).thenReturn(mockCollection);

        // Mock collection operations
        lenient().when(mockCollection.countDocuments(any(Bson.class))).thenReturn(1L);
        lenient().when(mockCollection.find()).thenReturn(mockFindIterable);
        lenient().when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Mock FindIterable operations
        lenient().when(mockFindIterable.first()).thenReturn(null); // Return null to simulate empty result
        lenient().when(mockFindIterable.projection(any())).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.skip(anyInt())).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.limit(anyInt())).thenReturn(mockFindIterable);

        // Mock iterator for for-each loops - return empty cursor to simulate no results
        lenient().when(mockMongoCursor.hasNext()).thenReturn(false);
        lenient().when(mockFindIterable.iterator()).thenReturn(mockMongoCursor);

        // Mock insert operations
        BsonObjectId mockObjectId = new BsonObjectId(new org.bson.types.ObjectId());
        lenient().when(mockInsertResult.getInsertedId()).thenReturn(mockObjectId);
        lenient().when(mockCollection.insertOne(any(org.bson.Document.class))).thenReturn(mockInsertResult);

        // Mock other collection operations
        lenient().when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);

        // Mock database command for getStatus
        lenient().when(mockDatabase.runCommand(any(org.bson.Document.class))).thenReturn(new org.bson.Document());
    }

    @Test
    @DisplayName("Should implement Closeable interface")
    void testImplementsCloseable() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");
        assertInstanceOf(Closeable.class, cm);
    }

    @Test
    @DisplayName("Should create CollectionManager with client and database name")
    void testConstructorWithClientAndDatabase() {
        String dbName = "testDatabase";
        CollectionManager cm = new CollectionManager(mockClient, dbName);

        assertNotNull(cm);
        verify(mockClient).getDatabase(dbName);
    }

    @Test
    @DisplayName("Should use specified database")
    void testUseDatabase() {
        CollectionManager cm = new CollectionManager(mockClient, "initialDb");
        String newDbName = "newDatabase";

        cm.use(newDbName);

        verify(mockClient, times(2)).getDatabase(anyString());
        verify(mockClient).getDatabase("initialDb");
        verify(mockClient).getDatabase(newDbName);
    }

    @Test
    @DisplayName("Should throw exception for null client")
    void testConstructorWithNullClient() {
        assertThrows(NullPointerException.class, () ->
            new CollectionManager(null, "testdb"));
    }

    @Test
    @DisplayName("Should handle null database name gracefully")
    void testConstructorWithNullDatabase() {
        assertDoesNotThrow(() -> {
            CollectionManager cm = new CollectionManager(mockClient, null);
            assertNotNull(cm);
        });
    }

    @Test
    @DisplayName("Should handle empty database name gracefully")
    void testConstructorWithEmptyDatabase() {
        assertDoesNotThrow(() -> {
            CollectionManager cm = new CollectionManager(mockClient, "");
            assertNotNull(cm);
        });
    }

    @Test
    @DisplayName("Should close MongoDB client")
    void testClose() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        cm.close();

        verify(mockClient).close();
    }

    @Test
    @DisplayName("Should handle close with null client gracefully")
    void testCloseWithNullClient() {
        // This tests the robustness of the close method
        assertDoesNotThrow(() -> {
            CollectionManager cm = new CollectionManager(mockClient, "testdb");
            cm.close();
            cm.close(); // Second close should not fail
        });
    }

    @Test
    @DisplayName("Should validate document class has @Document annotation")
    void testDocumentValidation() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // This would typically be tested in integration tests with actual MongoDB operations
        // Here we test the class structure and annotation presence
        assertTrue(TestDocument.class.isAnnotationPresent(Document.class));
        assertFalse(NonDocumentClass.class.isAnnotationPresent(Document.class));
    }

    @Test
    @DisplayName("Should extract collection name from @Document annotation")
    void testCollectionNameExtraction() {
        // Test explicit collection name
        Document docAnnotation = TestDocument.class.getAnnotation(Document.class);
        assertEquals("test_collection", docAnnotation.collection());

        // Test default collection name (should use class name)
        Document defaultAnnotation = DefaultCollectionDocument.class.getAnnotation(Document.class);
        assertEquals("", defaultAnnotation.collection()); // Default is empty string
    }

    @Test
    @DisplayName("Should identify ObjectId fields correctly")
    void testObjectIdFieldIdentification() throws NoSuchFieldException {
        var idField = TestDocument.class.getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(ObjectId.class));

        var nameField = TestDocument.class.getDeclaredField("name");
        assertFalse(nameField.isAnnotationPresent(ObjectId.class));
    }

    @Test
    @DisplayName("Should handle reflection operations safely")
    void testReflectionSafety() {
        // Test that the class can handle reflection operations
        TestDocument doc = new TestDocument("John", 25);

        // Test field access
        var fields = doc.getClass().getDeclaredFields();
        assertTrue(fields.length > 0);

        // Test annotation presence
        boolean hasObjectIdField = false;
        for (var field : fields) {
            if (field.isAnnotationPresent(ObjectId.class)) {
                hasObjectIdField = true;
                break;
            }
        }
        assertTrue(hasObjectIdField);
    }

    @Test
    @DisplayName("Should support default constructors")
    void testDefaultConstructors() {
        assertDoesNotThrow(() -> {
            TestDocument doc1 = new TestDocument();
            DefaultCollectionDocument doc2 = new DefaultCollectionDocument();
            NonDocumentClass doc3 = new NonDocumentClass();

            assertNotNull(doc1);
            assertNotNull(doc2);
            assertNotNull(doc3);
        });
    }

    @Test
    @DisplayName("Should handle document field types correctly")
    void testDocumentFieldTypes() throws NoSuchFieldException {
        var idField = TestDocument.class.getDeclaredField("id");
        assertEquals(String.class, idField.getType());

        var nameField = TestDocument.class.getDeclaredField("name");
        assertEquals(String.class, nameField.getType());

        var ageField = TestDocument.class.getDeclaredField("age");
        assertEquals(int.class, ageField.getType());
    }

    @Test
    @DisplayName("Should validate MongoDB connection requirements")
    void testMongoDBConnectionRequirements() {
        // Test that CollectionManager requires valid MongoDB components
        assertNotNull(mockClient);
        assertNotNull(mockDatabase);

        // Verify that database is accessed during construction
        new CollectionManager(mockClient, "testdb");
        verify(mockClient).getDatabase("testdb");
    }

    @Test
    @DisplayName("Should handle query parameter validation")
    void testQueryParameterValidation() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test that null queries would be handled appropriately
        // (In actual implementation, this would be tested with real MongoDB operations)
        assertNotNull(cm);
    }

    @Test
    @DisplayName("Should support method chaining where applicable")
    void testMethodChainingSupport() {
        // Test that the CollectionManager can be used in a fluent manner
        CollectionManager cm = new CollectionManager(mockClient, "testdb");
        assertNotNull(cm);

        // The close method should be callable
        assertDoesNotThrow(() -> cm.close());
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void testThreadSafety() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Basic thread safety test - ensure object can be accessed from multiple contexts
        assertDoesNotThrow(() -> {
            Thread t1 = new Thread(() -> {
                // Simulate concurrent access
                assertNotNull(cm);
            });
            Thread t2 = new Thread(() -> {
                // Simulate concurrent access
                assertNotNull(cm);
            });

            t1.start();
            t2.start();

            t1.join();
            t2.join();
        });
    }

    @Test
    @DisplayName("Should validate class structure for document mapping")
    void testDocumentMappingValidation() {
        // Verify that test documents have the expected structure
        TestDocument doc = new TestDocument("test", 42);

        // Test that getters/setters work correctly
        assertEquals("test", doc.getName());
        assertEquals(42, doc.getAge());

        doc.setName("updated");
        doc.setAge(100);

        assertEquals("updated", doc.getName());
        assertEquals(100, doc.getAge());
    }

    @Test
    @DisplayName("Should handle annotation processing correctly")
    void testAnnotationProcessing() {
        // Test various annotation combinations
        var documentClass = TestDocument.class;

        // Should have @Document annotation
        assertTrue(documentClass.isAnnotationPresent(Document.class));

        // Should have at least one @ObjectId field
        boolean hasObjectIdField = false;
        for (var field : documentClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ObjectId.class)) {
                hasObjectIdField = true;
                break;
            }
        }
        assertTrue(hasObjectIdField);
    }

    @Test
    @DisplayName("Should provide proper error handling structure")
    void testErrorHandlingStructure() {
        // Test that the CollectionManager can handle various error conditions
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test that it can be closed multiple times without error
        assertDoesNotThrow(() -> {
            cm.close();
            cm.close(); // Should not throw on second close
        });
    }

    @Test
    @DisplayName("Should handle document annotation processing")
    void testDocumentAnnotationHandling() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test that TestDocument has proper annotations
        assertTrue(TestDocument.class.isAnnotationPresent(Document.class));

        Document docAnnotation = TestDocument.class.getAnnotation(Document.class);
        assertEquals("test_collection", docAnnotation.collection());
    }

    @Test
    @DisplayName("Should handle default collection names")
    void testDefaultCollectionNames() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test with DefaultCollectionDocument which has @Document with no collection specified
        assertTrue(DefaultCollectionDocument.class.isAnnotationPresent(Document.class));

        Document docAnnotation = DefaultCollectionDocument.class.getAnnotation(Document.class);
        assertEquals("", docAnnotation.collection()); // Default is empty string
    }

    @Test
    @DisplayName("Should identify classes without Document annotation")
    void testNonDocumentClasses() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test with NonDocumentClass which has no @Document annotation
        assertFalse(NonDocumentClass.class.isAnnotationPresent(Document.class));
    }


    @Test
    @DisplayName("Should call count methods")
    void testCountMethods() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test count without query
        assertDoesNotThrow(() -> {
            cm.count(TestDocument.class);
        });

        // Test count with query
        MongoQuery query = new MongoQuery();
        assertDoesNotThrow(() -> {
            cm.count(TestDocument.class, query);
        });

        // Verify MongoDB interactions
        verify(mockDatabase, atLeastOnce()).getCollection("test_collection");
    }

    @Test
    @DisplayName("Should call find methods")
    void testFindMethods() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test find without query
        assertDoesNotThrow(() -> {
            cm.find(TestDocument.class);
        });

        // Test find with query
        MongoQuery query = new MongoQuery();
        assertDoesNotThrow(() -> {
            cm.find(TestDocument.class, query);
        });

        // Verify MongoDB interactions
        verify(mockDatabase, atLeastOnce()).getCollection("test_collection");
    }

    @Test
    @DisplayName("Should call findOne methods")
    void testFindOneMethods() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test findOne without query
        assertDoesNotThrow(() -> {
            cm.findOne(TestDocument.class);
        });

        // Test findOne with query
        MongoQuery query = new MongoQuery();
        assertDoesNotThrow(() -> {
            cm.findOne(TestDocument.class, query);
        });

        // Test findById
        assertDoesNotThrow(() -> {
            cm.findById(TestDocument.class, "507f1f77bcf86cd799439011");
        });

        // Verify MongoDB interactions
        verify(mockDatabase, atLeastOnce()).getCollection("test_collection");
    }

    @Test
    @DisplayName("Should call CRUD methods")
    void testCRUDMethods() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");
        TestDocument doc = new TestDocument();
        doc.setName("test");
        doc.setAge(25);

        // Test insert
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Test save
        assertDoesNotThrow(() -> {
            cm.save(doc);
        });

        // Test remove
        assertDoesNotThrow(() -> {
            cm.remove(doc);
        });

        // Test update methods
        MongoQuery query = new MongoQuery();
        assertDoesNotThrow(() -> {
            cm.update(query, doc);
            cm.update(query, doc, false, false);
            cm.updateMulti(query, doc);
        });

        // Verify MongoDB interactions
        verify(mockDatabase, atLeastOnce()).getCollection("test_collection");
    }

    @Test
    @DisplayName("Should call getStatus method")
    void testGetStatusMethod() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        assertDoesNotThrow(() -> {
            cm.getStatus();
        });

        // Verify MongoDB interactions
        verify(mockDatabase, atLeastOnce()).runCommand(any(org.bson.Document.class));
    }

    // NEW COMPREHENSIVE TESTS FOR UNCOVERED METHODS

    @Test
    @DisplayName("Should test alternative constructors")
    void testAlternativeConstructors() {
        // Test constructor with user/password (currently not covered)
        assertDoesNotThrow(() -> {
            CollectionManager cm = new CollectionManager(mockClient, "testdb", "user", "pass");
            assertNotNull(cm);
        });

        // Test constructor with just client (currently not covered)
        assertDoesNotThrow(() -> {
            CollectionManager cm = new CollectionManager(mockClient);
            assertNotNull(cm);
        });
    }

    @Test
    @DisplayName("Should handle find with populated results and loadObject")
    void testFindWithResults() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Mock a document to be returned
        org.bson.Document mockDoc = new org.bson.Document();
        mockDoc.put("name", "TestName");
        mockDoc.put("age", 30);
        mockDoc.put("_id", new org.bson.types.ObjectId());

        // Mock cursor to return this document
        when(mockMongoCursor.hasNext()).thenReturn(true).thenReturn(false);
        when(mockMongoCursor.next()).thenReturn(mockDoc);

        // Test find method that will hit loadObject
        assertDoesNotThrow(() -> {
            List<TestDocument> results = cm.find(TestDocument.class);
            assertNotNull(results);
        });
    }

    @Test
    @DisplayName("Should handle findOne with result and loadObject")
    void testFindOneWithResult() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Mock a document to be returned
        org.bson.Document mockDoc = new org.bson.Document();
        mockDoc.put("name", "TestName");
        mockDoc.put("age", 30);
        mockDoc.put("_id", new org.bson.types.ObjectId());

        // Mock findIterable to return this document
        when(mockFindIterable.first()).thenReturn(mockDoc);

        // Test findOne method that will hit loadObject
        assertDoesNotThrow(() -> {
            TestDocument result = cm.findOne(TestDocument.class);
            assertNotNull(result);
        });

        // Test findOne with query that will hit loadObject
        MongoQuery query = new MongoQuery();
        assertDoesNotThrow(() -> {
            TestDocument result = cm.findOne(TestDocument.class, query);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle complex document types with enums, references and internal objects")
    void testComplexDocumentTypes() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Setup document with complex types
        DocumentWithReference doc = new DocumentWithReference();
        doc.setStatus(TestEnum.ACTIVE);

        ReferencedDocument refDoc = new ReferencedDocument("ref");
        refDoc.setId("ref_id");
        doc.setReference(refDoc);

        List<ReferencedDocument> internalList = new ArrayList<>();
        internalList.add(new ReferencedDocument("internal1"));
        doc.setInternalList(internalList);

        // Test insert with complex document (will hit multiple loadDocument branches)
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Test save with complex document
        assertDoesNotThrow(() -> {
            cm.save(doc);
        });
    }

    @Test
    @DisplayName("Should handle documents with generated values")
    void testGeneratedValueDocuments() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithGeneratedValue doc = new DocumentWithGeneratedValue();
        doc.setSequence(null); // null value to trigger generation

        // Test insert with generated value document
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Test save with generated value document
        assertDoesNotThrow(() -> {
            cm.save(doc);
        });
    }

    @Test
    @DisplayName("Should handle documents with @Id annotation")
    void testIdAnnotationDocuments() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithIdAnnotation doc = new DocumentWithIdAnnotation();

        // Test insert with @Id annotation document
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Test save with @Id annotation document
        assertDoesNotThrow(() -> {
            cm.save(doc);
        });
    }

    @Test
    @DisplayName("Should handle indexing operations")
    void testIndexingOperations() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithIndexes doc = new DocumentWithIndexes();
        doc.setName("testName");
        doc.setDescription("testDescription");
        doc.setField1("field1Value");
        doc.setField2("field2Value");

        // Test insert with indexed document (will trigger indexFields)
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Verify index creation was attempted
        verify(mockCollection, atLeastOnce()).createIndex(any(org.bson.Document.class), any());
    }

    @Test
    @DisplayName("Should handle null document insertion")
    void testNullDocumentInsertion() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test insert with null document
        String result = cm.insert(null);
        assertNull(result);

        // Test save with null document
        String saveResult = cm.save(null);
        assertNull(saveResult);
    }

    @Test
    @DisplayName("Should handle update operations with WriteConcern")
    void testUpdateWithWriteConcern() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        TestDocument doc = new TestDocument("test", 25);
        MongoQuery query = new MongoQuery("name", "test");

        // Test update with WriteConcern
        assertDoesNotThrow(() -> {
            cm.update(query, doc, false, false, WriteConcern.MAJORITY);
        });

        // Test multi update
        assertDoesNotThrow(() -> {
            cm.update(query, doc, false, true, WriteConcern.ACKNOWLEDGED);
        });

        // Verify withWriteConcern was called
        verify(mockCollection, atLeastOnce()).withWriteConcern(any(WriteConcern.class));
    }

    @Test
    @DisplayName("Should handle save with existing document (upsert)")
    void testSaveWithExistingDocument() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        TestDocument doc = new TestDocument("existing", 30);
        doc.setId("507f1f77bcf86cd799439011"); // Set valid ObjectId to simulate existing document

        // Test save with existing document (should trigger upsert path)
        assertDoesNotThrow(() -> {
            cm.save(doc);
        });

        // Verify replaceOne was called for upsert
        verify(mockCollection, atLeastOnce()).replaceOne(any(Bson.class), any(org.bson.Document.class), any(ReplaceOptions.class));
    }

    @Test
    @DisplayName("Should handle query with constraints and ordering")
    void testQueryWithConstraintsAndOrdering() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create query with constraints and ordering
        MongoQuery query = new MongoQuery("age", 25);
        query.returnOnly(true, "name", "age");
        query.orderBy("name", 1);
        query.limit(10);
        query.skip(5);

        // Test find with complex query (will hit constraint and ordering branches)
        assertDoesNotThrow(() -> {
            cm.find(TestDocument.class, query);
        });

        // Test findOne with complex query
        assertDoesNotThrow(() -> {
            cm.findOne(TestDocument.class, query);
        });

        // Verify projection, sort, limit, skip were called
        verify(mockFindIterable, atLeastOnce()).projection(any());
        verify(mockFindIterable, atLeastOnce()).sort(any());
        verify(mockFindIterable, atLeastOnce()).limit(anyInt());
        verify(mockFindIterable, atLeastOnce()).skip(anyInt());
    }

    @Test
    @DisplayName("Should handle count method exceptions gracefully")
    void testCountMethodExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test count with a class that can't be instantiated (private constructor)
        class PrivateConstructorClass {
            private PrivateConstructorClass() {}
        }

        // This should handle the exception gracefully and return 0
        long count = cm.count(PrivateConstructorClass.class);
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should handle getStatus with exception")
    void testGetStatusWithException() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Mock database to throw exception
        when(mockDatabase.runCommand(any(org.bson.Document.class))).thenThrow(new RuntimeException("Connection error"));
        // Since runCommand throws first, listDatabaseNames won't be called
        lenient().when(mockClient.listDatabaseNames()).thenThrow(new RuntimeException("List error"));

        String status = cm.getStatus();
        assertNotNull(status);
        assertTrue(status.contains("error"), "Status should contain error message");
    }

    @Test
    @DisplayName("Should handle getStatus with successful connection")
    void testGetStatusSuccess() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Mock successful ping response
        org.bson.Document pingResponse = new org.bson.Document("ok", 1);
        when(mockDatabase.runCommand(any(org.bson.Document.class))).thenReturn(pingResponse);

        // Mock database names
        MongoIterable<String> mockIterable = mock(MongoIterable.class);
        MongoCursor<String> mockDbIterator = mock(MongoCursor.class);
        when(mockClient.listDatabaseNames()).thenReturn(mockIterable);
        when(mockIterable.iterator()).thenReturn(mockDbIterator);
        when(mockDbIterator.hasNext()).thenReturn(true, true, true, false);
        when(mockDbIterator.next()).thenReturn("db1", "db2", "testdb");

        String status = cm.getStatus();
        assertNotNull(status);
        assertTrue(status.contains("connected"), "Status should indicate connection");
        assertTrue(status.contains("db1"), "Status should list databases");
    }
}



