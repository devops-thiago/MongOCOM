package com.arquivolivre.mongocom.management;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.annotations.GeneratedValue;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import com.arquivolivre.mongocom.utils.DateGenerator;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionManager Edge Cases and Error Conditions")
class CollectionManagerEdgeCaseTest {

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

    // Edge case test classes

    @Document(collection = "multiple_objectid")
    static class DocumentWithMultipleObjectIds {
        @ObjectId
        private String id1;

        @ObjectId
        private String id2; // Multiple @ObjectId fields to test warning

        public DocumentWithMultipleObjectIds() {}

        public String getId1() { return id1; }
        public void setId1(String id1) { this.id1 = id1; }
        public String getId2() { return id2; }
        public void setId2(String id2) { this.id2 = id2; }
    }

    @Document(collection = "no_objectid")
    static class DocumentWithoutObjectId {
        private String name;
        private int value;

        public DocumentWithoutObjectId() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    @Document(collection = "generated_edge_cases")
    static class DocumentWithGeneratedEdgeCases {
        @ObjectId
        private String id;

        @GeneratedValue(generator = IntegerGenerator.class, update = false)
        private Integer nonUpdateableNumber = 5; // Non-zero, non-updateable

        @GeneratedValue(generator = IntegerGenerator.class, update = true)
        private Integer updateableNumber = 10; // Non-zero, updateable

        @GeneratedValue(generator = IntegerGenerator.class)
        private Integer nullNumber; // null value

        public DocumentWithGeneratedEdgeCases() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Integer getNonUpdateableNumber() { return nonUpdateableNumber; }
        public void setNonUpdateableNumber(Integer nonUpdateableNumber) { this.nonUpdateableNumber = nonUpdateableNumber; }
        public Integer getUpdateableNumber() { return updateableNumber; }
        public void setUpdateableNumber(Integer updateableNumber) { this.updateableNumber = updateableNumber; }
        public Integer getNullNumber() { return nullNumber; }
        public void setNullNumber(Integer nullNumber) { this.nullNumber = nullNumber; }
    }

    @Document(collection = "primitives_doc")
    static class DocumentWithPrimitives {
        @ObjectId
        private String id;

        private int primitiveInt;
        private boolean primitiveBoolean;
        private double primitiveDouble;
        private String nullableString;

        public DocumentWithPrimitives() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getPrimitiveInt() { return primitiveInt; }
        public void setPrimitiveInt(int primitiveInt) { this.primitiveInt = primitiveInt; }
        public boolean isPrimitiveBoolean() { return primitiveBoolean; }
        public void setPrimitiveBoolean(boolean primitiveBoolean) { this.primitiveBoolean = primitiveBoolean; }
        public double getPrimitiveDouble() { return primitiveDouble; }
        public void setPrimitiveDouble(double primitiveDouble) { this.primitiveDouble = primitiveDouble; }
        public String getNullableString() { return nullableString; }
        public void setNullableString(String nullableString) { this.nullableString = nullableString; }
    }

    @BeforeEach
    void setUp() {
        // Mock the database and client
        lenient().when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
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

        // Mock iterator
        lenient().when(mockMongoCursor.hasNext()).thenReturn(false);
        lenient().when(mockFindIterable.iterator()).thenReturn(mockMongoCursor);

        // Mock insert operations
        BsonObjectId mockObjectId = new BsonObjectId(new org.bson.types.ObjectId());
        lenient().when(mockInsertResult.getInsertedId()).thenReturn(mockObjectId);
        lenient().when(mockCollection.insertOne(any(org.bson.Document.class))).thenReturn(mockInsertResult);

        // Mock other operations
        lenient().when(mockCollection.withWriteConcern(any())).thenReturn(mockCollection);
        lenient().when(mockDatabase.runCommand(any(org.bson.Document.class))).thenReturn(new org.bson.Document());
        lenient().when(mockCollection.createIndex(any(org.bson.Document.class), any())).thenReturn("mockIndex");
        lenient().when(mockCollection.createIndex(any(org.bson.Document.class))).thenReturn("mockIndex");
    }

    @Test
    @DisplayName("Should handle document with multiple @ObjectId fields")
    void testMultipleObjectIdFields() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithMultipleObjectIds doc = new DocumentWithMultipleObjectIds();
        // Use valid 24-character hex strings for ObjectIds
        doc.setId1("507f1f77bcf86cd799439011");
        doc.setId2("507f1f77bcf86cd799439012");

        // This should trigger the warning about multiple @ObjectId fields
        // and use the first one found
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle document without @ObjectId fields")
    void testDocumentWithoutObjectId() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithoutObjectId doc = new DocumentWithoutObjectId();
        doc.setName("test");
        doc.setValue(42);

        // Should handle document without @ObjectId field gracefully
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle generated value edge cases")
    void testGeneratedValueEdgeCases() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithGeneratedEdgeCases doc = new DocumentWithGeneratedEdgeCases();

        // Test various combinations of generated value scenarios
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Test save as well
        assertDoesNotThrow(() -> {
            cm.save(doc);
        });
    }

    @Test
    @DisplayName("Should handle primitive fields with null values in loadObject")
    void testPrimitiveFieldsWithNullValues() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with null values for primitive fields
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("primitiveInt", null); // null for primitive field
        mongoDoc.put("primitiveBoolean", null); // null for primitive field
        mongoDoc.put("primitiveDouble", null); // null for primitive field
        mongoDoc.put("nullableString", null); // null for nullable field

        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Test findOne - will exercise loadObject with null primitive handling
        assertDoesNotThrow(() -> {
            DocumentWithPrimitives result = cm.findOne(DocumentWithPrimitives.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle non-null values for all field types in loadObject")
    void testLoadObjectWithNonNullValues() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with non-null values
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("primitiveInt", 42);
        mongoDoc.put("primitiveBoolean", true);
        mongoDoc.put("primitiveDouble", 3.14);
        mongoDoc.put("nullableString", "not null");

        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Test findOne - will exercise loadObject with non-null value assignment
        assertDoesNotThrow(() -> {
            DocumentWithPrimitives result = cm.findOne(DocumentWithPrimitives.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle documents with @Id annotation but empty value")
    void testIdAnnotationWithEmptyValue() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "id_empty_test")
        class DocumentWithEmptyId {
            @ObjectId
            private String objectId;

            @Id(autoIncrement = true, generator = IntegerGenerator.class)
            private String customId = ""; // Empty string to test empty condition

            public DocumentWithEmptyId() {}

            public String getObjectId() { return objectId; }
            public void setObjectId(String objectId) { this.objectId = objectId; }
            public String getCustomId() { return customId; }
            public void setCustomId(String customId) { this.customId = customId; }
        }

        DocumentWithEmptyId doc = new DocumentWithEmptyId();

        // Should handle @Id with empty value gracefully
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle documents with @Id annotation and non-empty value")
    void testIdAnnotationWithNonEmptyValue() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "id_nonempty_test")
        class DocumentWithNonEmptyId {
            @ObjectId
            private String objectId;

            @Id(autoIncrement = true, generator = IntegerGenerator.class)
            private String customId = "existing_id"; // Non-empty value

            public DocumentWithNonEmptyId() {}

            public String getObjectId() { return objectId; }
            public void setObjectId(String objectId) { this.objectId = objectId; }
            public String getCustomId() { return customId; }
            public void setCustomId(String customId) { this.customId = customId; }
        }

        DocumentWithNonEmptyId doc = new DocumentWithNonEmptyId();

        // Should handle @Id with non-empty value differently
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle exception in field access during loadDocument")
    void testFieldAccessException() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create a document class that will cause field access issues
        @Document(collection = "field_access_test")
        class ProblematicDocument {
            @ObjectId
            private String id;

            // This field might cause access issues in some scenarios
            private final String immutableField = "constant";

            public ProblematicDocument() {}

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getImmutableField() { return immutableField; }
        }

        ProblematicDocument doc = new ProblematicDocument();

        // Should handle potential field access exceptions gracefully
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle zero values in generated value scenarios")
    void testZeroValueGeneratedScenarios() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "zero_test")
        class DocumentWithZeroValues {
            @ObjectId
            private String id;

            @GeneratedValue(generator = IntegerGenerator.class, update = false)
            private Integer zeroNotUpdateable = 0; // Zero value, not updateable

            @GeneratedValue(generator = IntegerGenerator.class, update = true)
            private Integer zeroUpdateable = 0; // Zero value, updateable

            public DocumentWithZeroValues() {}

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public Integer getZeroNotUpdateable() { return zeroNotUpdateable; }
            public void setZeroNotUpdateable(Integer zeroNotUpdateable) { this.zeroNotUpdateable = zeroNotUpdateable; }
            public Integer getZeroUpdateable() { return zeroUpdateable; }
            public void setZeroUpdateable(Integer zeroUpdateable) { this.zeroUpdateable = zeroUpdateable; }
        }

        DocumentWithZeroValues doc = new DocumentWithZeroValues();

        // Test to hit the zero value branches in reflectGeneratedValue
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle non-number values in generated value scenarios")
    void testNonNumberGeneratedValues() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "non_number_test")
        class DocumentWithNonNumberGenerated {
            @ObjectId
            private String id;

            @GeneratedValue(generator = DateGenerator.class, update = false)
            private Date dateField = new Date(); // Non-number generated value

            public DocumentWithNonNumberGenerated() {}

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public Date getDateField() { return dateField; }
            public void setDateField(Date dateField) { this.dateField = dateField; }
        }

        DocumentWithNonNumberGenerated doc = new DocumentWithNonNumberGenerated();

        // Test to hit the non-Number branch in reflectGeneratedValue
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle query with zero limit and skip")
    void testQueryWithZeroLimitAndSkip() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create query with zero limit and skip (should not apply these operations)
        MongoQuery query = new MongoQuery("field", "value");
        query.limit(0); // Zero limit - should not call limit()
        query.skip(0);  // Zero skip - should not call skip()

        // Test find - should not call limit() or skip() on FindIterable
        assertDoesNotThrow(() -> {
            cm.find(DocumentWithoutObjectId.class, query);
        });

        // Verify limit and skip were NOT called for zero values
        verify(mockFindIterable, never()).limit(0);
        verify(mockFindIterable, never()).skip(0);
    }

    @Test
    @DisplayName("Should handle query with positive limit and skip")
    void testQueryWithPositiveLimitAndSkip() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create query with positive limit and skip
        MongoQuery query = new MongoQuery("field", "value");
        query.limit(10); // Positive limit - should call limit()
        query.skip(5);   // Positive skip - should call skip()

        // Test find - should call limit() and skip() on FindIterable
        assertDoesNotThrow(() -> {
            cm.find(DocumentWithoutObjectId.class, query);
        });

        // Verify limit and skip were called for positive values
        verify(mockFindIterable, atLeastOnce()).limit(10);
        verify(mockFindIterable, atLeastOnce()).skip(5);
    }

    @Test
    @DisplayName("Should handle query with null constraints")
    void testQueryWithNullConstraints() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create empty query (null constraints and orderBy)
        MongoQuery query = new MongoQuery();
        // Don't set any constraints or ordering

        // Test find - should not call projection() or sort()
        assertDoesNotThrow(() -> {
            cm.find(DocumentWithoutObjectId.class, query);
        });

        // Test findOne - should not call projection()
        assertDoesNotThrow(() -> {
            cm.findOne(DocumentWithoutObjectId.class, query);
        });
    }

    @Test
    @DisplayName("Should handle exception during object instantiation in find")
    void testInstantiationExceptionInFind() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Mock to return a document so we hit the instantiation in the loop
        org.bson.Document mockDoc = new org.bson.Document();
        mockDoc.put("_id", new org.bson.types.ObjectId());

        lenient().when(mockMongoCursor.hasNext()).thenReturn(true).thenReturn(false);
        lenient().when(mockMongoCursor.next()).thenReturn(mockDoc);

        // Test with abstract class that can't be instantiated
        abstract class AbstractClass {
            @ObjectId
            private String id;
        }

        // Should handle InstantiationException in the find loop
        List<?> results = cm.find(AbstractClass.class);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle reflection exceptions in getFieldsByAnnotation")
    void testGetFieldsByAnnotationEdgeCases() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        DocumentWithMultipleObjectIds doc = new DocumentWithMultipleObjectIds();

        // Test insert which will call getFieldsByAnnotation for @ObjectId
        // This should find multiple fields and trigger the warning
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle @Id annotation with autoIncrement false")
    void testIdAnnotationAutoIncrementFalse() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "id_no_increment")
        class DocumentWithNoAutoIncrement {
            @ObjectId
            private String objectId;

            @Id(autoIncrement = false, generator = IntegerGenerator.class)
            private Integer customId;

            public DocumentWithNoAutoIncrement() {}

            public String getObjectId() { return objectId; }
            public void setObjectId(String objectId) { this.objectId = objectId; }
            public Integer getCustomId() { return customId; }
            public void setCustomId(Integer customId) { this.customId = customId; }
        }

        DocumentWithNoAutoIncrement doc = new DocumentWithNoAutoIncrement();

        // Test with autoIncrement = false (should not generate value)
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Document(collection = "empty_list_test")
    static class DocumentWithEmptyList {
        @ObjectId
        private String id;

        @Internal
        private List<DocumentWithoutObjectId> internalList;

        private List<String> regularList;

        public DocumentWithEmptyList() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public List<DocumentWithoutObjectId> getInternalList() { return internalList; }
        public void setInternalList(List<DocumentWithoutObjectId> internalList) { this.internalList = internalList; }
        public List<String> getRegularList() { return regularList; }
        public void setRegularList(List<String> regularList) { this.regularList = regularList; }
    }

    @Test
    @DisplayName("Should handle loadObject with empty/null lists")
    void testLoadObjectWithEmptyLists() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create MongoDB document with empty lists
        org.bson.Document mongoDoc = new org.bson.Document();
        mongoDoc.put("_id", new org.bson.types.ObjectId());
        mongoDoc.put("internalList", new ArrayList<>()); // Empty list
        mongoDoc.put("regularList", new ArrayList<>()); // Empty list

        when(mockFindIterable.first()).thenReturn(mongoDoc);

        // Test findOne - will exercise loadObject with empty lists
        assertDoesNotThrow(() -> {
            DocumentWithEmptyList result = cm.findOne(DocumentWithEmptyList.class);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle insert without successful ObjectId field setting")
    void testInsertWithoutObjectIdFieldSetting() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Use a document without @ObjectId field
        DocumentWithoutObjectId doc = new DocumentWithoutObjectId();
        doc.setName("no_objectid_field");
        doc.setValue(100);

        // getFieldByAnnotation should return null for @ObjectId
        assertDoesNotThrow(() -> {
            String insertedId = cm.insert(doc);
            assertNotNull(insertedId); // Should still return inserted ID
        });
    }

    @Test
    @DisplayName("Should handle exception scenarios in reflection methods")
    void testReflectionMethodExceptions() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create document that might cause reflection issues
        @Document(collection = "reflection_exception_test")
        class ReflectionProblematicDocument {
            @ObjectId
            private String id;

            @GeneratedValue(generator = IntegerGenerator.class)
            private Integer generatedField;

            @Id(autoIncrement = true, generator = IntegerGenerator.class)
            private Integer idField;

            public ReflectionProblematicDocument() {}

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public Integer getGeneratedField() { return generatedField; }
            public void setGeneratedField(Integer generatedField) { this.generatedField = generatedField; }
            public Integer getIdField() { return idField; }
            public void setIdField(Integer idField) { this.idField = idField; }
        }

        ReflectionProblematicDocument doc = new ReflectionProblematicDocument();

        // Test operations that use reflection extensively
        assertDoesNotThrow(() -> {
            cm.insert(doc);
            cm.save(doc);
            cm.remove(doc);
        });
    }

    @Test
    @DisplayName("Should handle documents with all null fields")
    void testDocumentWithAllNullFields() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "all_null_test")
        class DocumentWithAllNulls {
            @ObjectId
            private String id; // null

            private String name; // null
            private Integer value; // null

            public DocumentWithAllNulls() {}

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public Integer getValue() { return value; }
            public void setValue(Integer value) { this.value = value; }
        }

        DocumentWithAllNulls doc = new DocumentWithAllNulls();
        // All fields are null by default

        // Test insert with all null fields
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });
    }

    @Test
    @DisplayName("Should handle MongoQuery getQuery() returning null")
    void testMongoQueryNullQuery() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        // Create a MongoQuery that might return null for getQuery()
        MongoQuery emptyQuery = new MongoQuery();

        // Test count with potentially null query
        assertDoesNotThrow(() -> {
            long count = cm.count(DocumentWithoutObjectId.class, emptyQuery);
            assertEquals(1, count); // Should return mocked count
        });
    }

    @Test
    @DisplayName("Should handle Index annotation with all parameter combinations")
    void testIndexAnnotationAllParameters() {
        CollectionManager cm = new CollectionManager(mockClient, "testdb");

        @Document(collection = "index_params_test")
        class DocumentWithIndexParams {
            @ObjectId
            private String id;

            @Index(value = "full_index", type = "", order = 1, unique = true, sparse = true, background = true, dropDups = true)
            private String fullParamsField;

            @Index(value = "", type = "", order = -1, unique = false, sparse = false, background = false, dropDups = false)
            private String minimalParamsField;

            public DocumentWithIndexParams() {}

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getFullParamsField() { return fullParamsField; }
            public void setFullParamsField(String fullParamsField) { this.fullParamsField = fullParamsField; }
            public String getMinimalParamsField() { return minimalParamsField; }
            public void setMinimalParamsField(String minimalParamsField) { this.minimalParamsField = minimalParamsField; }
        }

        DocumentWithIndexParams doc = new DocumentWithIndexParams();
        doc.setFullParamsField("full_value");
        doc.setMinimalParamsField("minimal_value");

        // Test insert - will exercise indexFields with all parameter combinations
        assertDoesNotThrow(() -> {
            cm.insert(doc);
        });

        // Verify index creation was called
        verify(mockCollection, atLeastOnce()).createIndex(any(org.bson.Document.class), any());
    }
}
