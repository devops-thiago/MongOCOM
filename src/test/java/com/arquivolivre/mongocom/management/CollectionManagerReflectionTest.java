package com.arquivolivre.mongocom.management;

import com.arquivolivre.mongocom.annotations.*;
import com.arquivolivre.mongocom.types.Action;
import com.arquivolivre.mongocom.types.TriggerType;
import com.arquivolivre.mongocom.utils.DateGenerator;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionManager Reflection and Complex Object Tests")
class CollectionManagerReflectionTest {

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

    // Complex test document classes for testing reflection and object loading

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
        lenient().when(mockFindIterable.first()).thenReturn(null);
        lenient().when(mockFindIterable.projection(any())).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.skip(anyInt())).thenReturn(mockFindIterable);
        lenient().when(mockFindIterable.limit(anyInt())).thenReturn(mockFindIterable);

        // Mock iterator for for-each loops
        lenient().when(mockMongoCursor.hasNext()).thenReturn(false);
        lenient().when(mockFindIterable.iterator()).thenReturn(mockMongoCursor);

        // Mock insert operations
        BsonObjectId mockObjectId = new BsonObjectId(new org.bson.types.ObjectId());
        lenient().when(mockInsertResult.getInsertedId()).thenReturn(mockObjectId);
        lenient().when(mockCollection.insertOne(any(org.bson.Document.class))).thenReturn(mockInsertResult);

        // Mock other collection operations
        lenient().when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
        lenient().when(mockDatabase.runCommand(any(org.bson.Document.class))).thenReturn(new org.bson.Document());

        // Mock collection operations for complex scenarios
        lenient().when(mockCollection.replaceOne(any(Bson.class), any(org.bson.Document.class), any(ReplaceOptions.class))).thenReturn(mock(UpdateResult.class));
        lenient().when(mockCollection.updateOne(any(), any(org.bson.Document.class))).thenReturn(mock(UpdateResult.class));
        lenient().when(mockCollection.updateMany(any(), any(org.bson.Document.class))).thenReturn(mock(UpdateResult.class));
        lenient().when(mockCollection.deleteOne(any())).thenReturn(mock(DeleteResult.class));
        lenient().when(mockCollection.createIndex(any(org.bson.Document.class), any())).thenReturn("mockIndex");
        lenient().when(mockCollection.createIndex(any(org.bson.Document.class))).thenReturn("mockIndex");
    }

    @Test
    @DisplayName("Should handle complex document insertion with all field types")
    void testComplexDocumentInsertion() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create complex document with all types of fields
        ComplexDocument doc = new ComplexDocument();
        doc.setRegularField("test");
        doc.setStatus(TestStatus.ACTIVE);
        doc.setPrimitiveNumber(42);
        doc.setIndexedField("indexed_value");

        // Add referenced document
        ReferencedDoc refDoc = new ReferencedDoc("reference_name");
        refDoc.setId("ref_id");
        doc.setReference(refDoc);

        // Add internal list
        List<InternalDoc> internalList = new ArrayList<>();
        internalList.add(new InternalDoc("internal1"));
        internalList.add(new InternalDoc("internal2"));
        doc.setInternalList(internalList);

        // Add single internal object
        doc.setSingleInternal(new InternalDoc("single_internal"));

        // Mock to return the reference document when saving
        when(mockInsertResult.getInsertedId()).thenReturn(new BsonObjectId(new org.bson.types.ObjectId()));

        // Test insert - this will exercise loadDocument with all field types
        assertDoesNotThrow(() -> {
            String insertedId = cm.insert(doc);
            assertNotNull(insertedId);
        });

        // Verify various collection operations were called
        verify(mockCollection, atLeastOnce()).insertOne(any(org.bson.Document.class));
        verify(mockCollection, atLeastOnce()).createIndex(any(org.bson.Document.class), any());
    }

    @Test
    @DisplayName("Should handle loadObject with complex MongoDB document")
    void testLoadObjectWithComplexDocument() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with various field types
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("regularField", "loaded_value");
        mongoDoc.put("status", "ACTIVE");
        mongoDoc.put("primitiveNumber", 100);

        // Add list of internal documents
        List<org.bson.Document> internalDocs = new ArrayList<>();
        internalDocs.add(new org.bson.Document("data", "internal1"));
        internalDocs.add(new org.bson.Document("data", "internal2"));
        mongoDoc.put("internalList", internalDocs);

        // Add single internal document
        mongoDoc.put("singleInternal", new org.bson.Document("data", "single"));

        // Add reference (ObjectId)
        mongoDoc.put("reference", new org.bson.types.ObjectId());

        // Mock findIterable to return this document for findOne
        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Mock findById for reference loading
        org.bson.Document refDoc = new org.bson.Document("name", "referenced_name");
        refDoc.put("_id", new org.bson.types.ObjectId()); // Add missing _id field
        when(mockFindIterable.first()).thenReturn(refDoc).thenReturn(mongoDoc);

        // Test findOne which will exercise loadObject with complex data
        assertDoesNotThrow(() -> {
            ComplexDocument result = cm.findOne(ComplexDocument.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle documents with generated values and various scenarios")
    void testGeneratedValueScenarios() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test document with null generated value
        DocumentWithNumbers doc1 = new DocumentWithNumbers();
        doc1.setUpdateableNumber(null); // Should trigger generation

        assertDoesNotThrow(() -> {
            cm.insert(doc1);
        });

        // Test document with zero value (should trigger generation)
        DocumentWithNumbers doc2 = new DocumentWithNumbers();
        doc2.setZeroNumber(0); // Should trigger generation for zero values

        assertDoesNotThrow(() -> {
            cm.insert(doc2);
        });

        // Test document with non-zero value and update=true
        DocumentWithNumbers doc3 = new DocumentWithNumbers();
        doc3.setUpdateableNumber(5); // Non-zero but updateable

        assertDoesNotThrow(() -> {
            cm.insert(doc3);
        });
    }

    @Test
    @DisplayName("Should handle findOne with complex query and loadObject")
    void testFindOneWithComplexQuery() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create complex MongoDB document
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("regularField", "found_value");
        mongoDoc.put("status", "PENDING");
        mongoDoc.put("primitiveNumber", null); // null primitive to test null handling

        // Mock findIterable to return this document
        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Create query with constraints
        MongoQuery query = new MongoQuery("regularField", "found_value");
        query.returnOnly(true, "regularField", "status");

        // Test findOne with query - will exercise loadObject with null primitive handling
        assertDoesNotThrow(() -> {
            ComplexDocument result = cm.findOne(ComplexDocument.class, query);
            assertNotNull(result);
        });

        // Verify projection was applied
        verify(mockFindIterable, atLeastOnce()).projection(any());
    }

    @Test
    @DisplayName("Should handle find with results that exercise loadObject")
    void testFindWithResultsAndLoadObject() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create multiple MongoDB documents
        org.bson.Document doc1 = new org.bson.Document();
        doc1.put("_id", new org.bson.types.ObjectId());
        doc1.put("regularField", "doc1");
        doc1.put("status", "ACTIVE");

        org.bson.Document doc2 = new org.bson.Document();
        doc2.put("_id", new org.bson.types.ObjectId());
        doc2.put("regularField", "doc2");
        doc2.put("status", "INACTIVE");

        // Mock cursor to return multiple documents
        when(mockMongoCursor.hasNext())
                .thenReturn(true)   // First document
                .thenReturn(true)   // Second document
                .thenReturn(false); // End iteration
        when(mockMongoCursor.next())
                .thenReturn(doc1)
                .thenReturn(doc2);

        // Test find - will exercise loadObject multiple times
        assertDoesNotThrow(() -> {
            List<ComplexDocument> results = cm.find(ComplexDocument.class);
            assertNotNull(results);
        });
    }

    @Test
    @DisplayName("Should handle save with document containing ObjectId")
    void testSaveWithObjectId() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        ComplexDocument doc = new ComplexDocument();
        doc.setId("507f1f77bcf86cd799439011"); // Set valid ObjectId to trigger upsert branch
        doc.setRegularField("save_test");

        // Mock insertResult to not return insertedId to test doc._id fallback
        lenient().when(mockInsertResult.getInsertedId()).thenReturn(null);

        assertDoesNotThrow(() -> {
            String savedId = cm.save(doc);
            // Should handle the case where insertedId is null
        });

        // Verify replaceOne was called for upsert
        verify(mockCollection, atLeastOnce()).replaceOne(any(Bson.class), any(org.bson.Document.class), any(ReplaceOptions.class));
    }

    @Test
    @DisplayName("Should handle insert with document containing empty ObjectId")
    void testInsertWithEmptyObjectId() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        ComplexDocument doc = new ComplexDocument();
        doc.setId(""); // Empty string ObjectId
        doc.setRegularField("test");

        assertDoesNotThrow(() -> {
            String insertedId = cm.insert(doc);
            assertNotNull(insertedId);
        });
    }

    @Test
    @DisplayName("Should handle insert with document containing non-empty ObjectId")
    void testInsertWithNonEmptyObjectId() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        ComplexDocument doc = new ComplexDocument();
        doc.setId("507f1f77bcf86cd799439011"); // Non-empty ObjectId
        doc.setRegularField("test");

        assertDoesNotThrow(() -> {
            String insertedId = cm.insert(doc);
            assertNotNull(insertedId);
        });
    }

    @Test
    @DisplayName("Should handle documents with enum fields in loadObject")
    void testLoadObjectWithEnumFields() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with enum string
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("status", "PENDING"); // String representation of enum
        mongoDoc.put("regularField", "enum_test");

        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Test findOne - will exercise loadObject with enum conversion
        assertDoesNotThrow(() -> {
            ComplexDocument result = cm.findOne(ComplexDocument.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle documents with reference fields in loadObject")
    void testLoadObjectWithReferenceFields() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with ObjectId reference
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("reference", new org.bson.types.ObjectId()); // Reference field
        mongoDoc.put("regularField", "reference_test");

        // Mock referenced document
        org.bson.Document refDoc = new org.bson.Document();
        refDoc.put("_id", new org.bson.types.ObjectId());
        refDoc.put("name", "referenced_name");

        when(mockFindIterable.first())
                .thenReturn(refDoc)     // First call for reference lookup
                .thenReturn(mongoDoc);  // Second call for main document

        // Test findOne - will exercise loadObject with reference loading
        assertDoesNotThrow(() -> {
            ComplexDocument result = cm.findOne(ComplexDocument.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle documents with internal list fields in loadObject")
    void testLoadObjectWithInternalListFields() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with internal list
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());

        // Add list of internal documents
        List<org.bson.Document> internalDocs = new ArrayList<>();
        org.bson.Document internalDoc1 = new org.bson.Document("data", "internal1");
        internalDoc1.put("_id", new org.bson.types.ObjectId());
        org.bson.Document internalDoc2 = new org.bson.Document("data", "internal2");
        internalDoc2.put("_id", new org.bson.types.ObjectId());
        internalDocs.add(internalDoc1);
        internalDocs.add(internalDoc2);
        mongoDoc.put("internalList", internalDocs);

        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Test findOne - will exercise loadObject with internal list processing
        assertDoesNotThrow(() -> {
            ComplexDocument result = cm.findOne(ComplexDocument.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle documents with regular list fields in loadObject")
    void testLoadObjectWithRegularListFields() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with regular list
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());

        List<String> regularList = List.of("item1", "item2", "item3");
        mongoDoc.put("regularList", regularList);

        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Test findOne - will exercise loadObject with regular list processing
        assertDoesNotThrow(() -> {
            DocumentWithRegularList result = cm.findOne(DocumentWithRegularList.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle save with document already having _id in loadDocument")
    void testSaveWithExistingId() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        ComplexDocument doc = new ComplexDocument();
        doc.setId("507f1f77bcf86cd799439011"); // Non-empty ObjectId
        doc.setRegularField("existing_doc");

        // Test save - should trigger upsert path in save method
        assertDoesNotThrow(() -> {
            String savedId = cm.save(doc);
            assertNotNull(savedId);
        });

        // Verify replaceOne was called (upsert)
        verify(mockCollection, atLeastOnce()).replaceOne(any(Bson.class), any(org.bson.Document.class), any(ReplaceOptions.class));
    }

    @Test
    @DisplayName("Should handle insert without insertedId but with _id in document")
    void testInsertWithoutInsertedId() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        ComplexDocument doc = new ComplexDocument();
        doc.setRegularField("no_inserted_id_test");

        // Mock insertResult to return null insertedId
        when(mockInsertResult.getInsertedId()).thenReturn(null);

        assertDoesNotThrow(() -> {
            String insertedId = cm.insert(doc);
            // Should handle null insertedId gracefully
        });
    }

    @Test
    @DisplayName("Should handle count with exception scenarios")
    void testCountWithExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test with class that will cause InstantiationException
        abstract class AbstractDocument {
            @ObjectId
            private String id;
        }

        // Count should handle exception and return 0
        long count = cm.count(AbstractDocument.class);
        assertEquals(0, count);

        // Test with query
        MongoQuery query = new MongoQuery("field", "value");
        long countWithQuery = cm.count(AbstractDocument.class, query);
        assertEquals(0, countWithQuery);
    }

    @Test
    @DisplayName("Should handle find with exception scenarios")
    void testFindWithExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test with class that will cause InstantiationException
        abstract class AbstractDocument {
            @ObjectId
            private String id;
        }

        // Find should handle exception and return empty list
        List<?> results = cm.find(AbstractDocument.class);
        assertNotNull(results);
        assertTrue(results.isEmpty());

        // Test with query
        MongoQuery query = new MongoQuery("field", "value");
        List<?> resultsWithQuery = cm.find(AbstractDocument.class, query);
        assertNotNull(resultsWithQuery);
        assertTrue(resultsWithQuery.isEmpty());
    }

    @Test
    @DisplayName("Should handle findOne with exception scenarios")
    void testFindOneWithExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Test with class that will cause InstantiationException
        abstract class AbstractDocument {
            @ObjectId
            private String id;
        }

        // FindOne should handle exception and return null
        Object result = cm.findOne(AbstractDocument.class);
        assertNull(result);

        // Test with query
        MongoQuery query = new MongoQuery("field", "value");
        Object resultWithQuery = cm.findOne(AbstractDocument.class, query);
        assertNull(resultWithQuery);
    }

    @Test
    @DisplayName("Should handle remove with exception scenarios")
    void testRemoveWithExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create document that will cause reflection exceptions
        ComplexDocument doc = new ComplexDocument();
        doc.setRegularField("test");

        // Test remove - should handle exceptions gracefully
        assertDoesNotThrow(() -> {
            cm.remove(doc);
        });

        verify(mockCollection, atLeastOnce()).deleteOne(any());
    }

    @Test
    @DisplayName("Should handle insert and save with exception scenarios")
    void testInsertSaveWithExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Document with potential reflection issues
        ComplexDocument doc = new ComplexDocument();
        doc.setRegularField("exception_test");

        // Test insert with potential exceptions
        assertDoesNotThrow(() -> {
            String insertedId = cm.insert(doc);
            // Should handle exceptions gracefully
        });

        // Test save with potential exceptions
        assertDoesNotThrow(() -> {
            String savedId = cm.save(doc);
            // Should handle exceptions gracefully
        });
    }

    @Test
    @DisplayName("Should handle update with exception scenarios")
    void testUpdateWithExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        ComplexDocument doc = new ComplexDocument();
        doc.setRegularField("update_test");

        MongoQuery query = new MongoQuery("field", "value");

        // Test update with potential exceptions
        assertDoesNotThrow(() -> {
            cm.update(query, doc);
            cm.update(query, doc, false, false);
            cm.update(query, doc, false, true);
            cm.update(query, doc, false, false, WriteConcern.MAJORITY);
            cm.updateMulti(query, doc);
        });
    }

    @Test
    @DisplayName("Should handle complex indexing scenarios")
    void testComplexIndexingScenarios() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Document with various index configurations
        @Document(collection = "complex_indexed")
        class ComplexIndexedDocument {
            @ObjectId
            private String id;

            @Index(value = "", type = "", order = 1) // Simple index
            private String simpleField;

            @Index(value = "compound", type = "", order = 1) // Compound index part 1
            private String compoundField1;

            @Index(value = "compound", type = "", order = -1) // Compound index part 2
            private String compoundField2;

            @Index(value = "", type = "text", order = 1) // Text index
            private String textField;

            @Index(value = "named_compound", type = "", order = 1, unique = true, sparse = true, background = false)
            private String namedCompoundField1;

            @Index(value = "named_compound", type = "", order = 1)
            private String namedCompoundField2;

            public ComplexIndexedDocument() {
            }

            public String getId() {
                return id;
            }

            public String getSimpleField() {
                return simpleField;
            }            public void setId(String id) {
                this.id = id;
            }

            public void setSimpleField(String simpleField) {
                this.simpleField = simpleField;
            }

            public String getCompoundField1() {
                return compoundField1;
            }

            public void setCompoundField1(String compoundField1) {
                this.compoundField1 = compoundField1;
            }

            public String getCompoundField2() {
                return compoundField2;
            }

            public void setCompoundField2(String compoundField2) {
                this.compoundField2 = compoundField2;
            }

            public String getTextField() {
                return textField;
            }

            public void setTextField(String textField) {
                this.textField = textField;
            }

            public String getNamedCompoundField1() {
                return namedCompoundField1;
            }

            public void setNamedCompoundField1(String namedCompoundField1) {
                this.namedCompoundField1 = namedCompoundField1;
            }

            public String getNamedCompoundField2() {
                return namedCompoundField2;
            }

            public void setNamedCompoundField2(String namedCompoundField2) {
                this.namedCompoundField2 = namedCompoundField2;
            }


        }

        ComplexIndexedDocument doc = new ComplexIndexedDocument();
        doc.setSimpleField("simple");
        doc.setCompoundField1("compound1");
        doc.setCompoundField2("compound2");
        doc.setTextField("text content");
        doc.setNamedCompoundField1("named1");
        doc.setNamedCompoundField2("named2");

        // Test insert - will exercise indexFields with all index types
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Verify multiple index creation calls
        // Based on the document, should create 4 indexes:
        // 1. Simple field index
        // 2. Text field index
        // 3. Compound index (compoundField1, compoundField2)
        // 4. Named compound index (namedCompoundField1, namedCompoundField2)
        verify(mockCollection, times(4)).createIndex(any(org.bson.Document.class), any());
    }

    @Test
    @DisplayName("Should handle field with underscore prefix in indexing")
    void testIndexingWithUnderscorePrefix() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "underscore_indexed")
        class UnderscoreIndexedDocument {
            @ObjectId
            private String id;

            @Index(value = "underscore_compound", order = 1)
            private String _prefixedField; // Field starting with underscore

            @Index(value = "underscore_compound", order = -1)
            private String regularField;

            public UnderscoreIndexedDocument() {
            }

            public String getId() {
                return id;
            }

            public String get_prefixedField() {
                return _prefixedField;
            }            public void setId(String id) {
                this.id = id;
            }

            public void set_prefixedField(String _prefixedField) {
                this._prefixedField = _prefixedField;
            }

            public String getRegularField() {
                return regularField;
            }

            public void setRegularField(String regularField) {
                this.regularField = regularField;
            }


        }

        UnderscoreIndexedDocument doc = new UnderscoreIndexedDocument();
        doc.set_prefixedField("underscore_value");
        doc.setRegularField("regular_value");

        // Test insert - will exercise indexFields with underscore handling
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        verify(mockCollection, atLeastOnce()).createIndex(any(org.bson.Document.class), any());
    }

    public enum TestStatus {
        ACTIVE, INACTIVE, PENDING
    }

    @Document(collection = "complex_document")
    static class ComplexDocument {
        @ObjectId
        private String id;

        @GeneratedValue(generator = IntegerGenerator.class)
        private Integer sequence;

        @GeneratedValue(generator = DateGenerator.class, update = true)
        private Date timestamp;

        @Id(autoIncrement = true, generator = IntegerGenerator.class)
        private Integer customId;

        @Reference
        private ReferencedDoc reference;

        @Internal
        private List<InternalDoc> internalList;

        @Internal
        private InternalDoc singleInternal;

        private TestStatus status; // Enum field

        @Index(value = "name_index", unique = true)
        private String indexedField;

        private Integer primitiveNumber;
        private String regularField;

        public ComplexDocument() {
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getSequence() {
            return sequence;
        }

        public void setSequence(Integer sequence) {
            this.sequence = sequence;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getCustomId() {
            return customId;
        }

        public void setCustomId(Integer customId) {
            this.customId = customId;
        }

        public ReferencedDoc getReference() {
            return reference;
        }

        public void setReference(ReferencedDoc reference) {
            this.reference = reference;
        }

        public List<InternalDoc> getInternalList() {
            return internalList;
        }

        public void setInternalList(List<InternalDoc> internalList) {
            this.internalList = internalList;
        }

        public InternalDoc getSingleInternal() {
            return singleInternal;
        }

        public void setSingleInternal(InternalDoc singleInternal) {
            this.singleInternal = singleInternal;
        }

        public TestStatus getStatus() {
            return status;
        }

        public void setStatus(TestStatus status) {
            this.status = status;
        }

        public String getIndexedField() {
            return indexedField;
        }

        public void setIndexedField(String indexedField) {
            this.indexedField = indexedField;
        }

        public Integer getPrimitiveNumber() {
            return primitiveNumber;
        }

        public void setPrimitiveNumber(Integer primitiveNumber) {
            this.primitiveNumber = primitiveNumber;
        }

        public String getRegularField() {
            return regularField;
        }

        public void setRegularField(String regularField) {
            this.regularField = regularField;
        }

        @Trigger(value = Action.ON_INSERT, when = TriggerType.BEFORE)
        public void beforeInsertTrigger() {
            // Trigger method to test method annotation processing
        }

        @Trigger(value = Action.ON_UPDATE, when = TriggerType.AFTER)
        public void afterUpdateTrigger() {
            // Another trigger method
        }
    }

    @Document(collection = "referenced_doc")
    static class ReferencedDoc {
        @ObjectId
        private String id;
        private String name;

        public ReferencedDoc() {
        }

        public ReferencedDoc(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Document(collection = "internal_doc")
    static class InternalDoc {
        @ObjectId
        private String id;
        private String data;

        public InternalDoc() {
        }

        public InternalDoc(String data) {
            this.data = data;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Document(collection = "numbered_document")
    static class DocumentWithNumbers {
        @ObjectId
        private String id;

        @GeneratedValue(generator = IntegerGenerator.class, update = true)
        private Integer updateableNumber;

        @GeneratedValue(generator = IntegerGenerator.class)
        private Integer zeroNumber = 0; // Test zero value handling

        public DocumentWithNumbers() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getUpdateableNumber() {
            return updateableNumber;
        }

        public void setUpdateableNumber(Integer updateableNumber) {
            this.updateableNumber = updateableNumber;
        }

        public Integer getZeroNumber() {
            return zeroNumber;
        }

        public void setZeroNumber(Integer zeroNumber) {
            this.zeroNumber = zeroNumber;
        }
    }

    // Document class with non-internal list for testing
    @Document(collection = "list_doc")
    static class DocumentWithRegularList {
        @ObjectId
        private String id;

        private List<String> regularList; // Not @Internal

        public DocumentWithRegularList() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getRegularList() {
            return regularList;
        }

        public void setRegularList(List<String> regularList) {
            this.regularList = regularList;
        }
    }
}
