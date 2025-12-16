/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>..
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
package com.arquivolivre.mongocom.management;

import com.arquivolivre.mongocom.annotations.GeneratedValue;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.utils.Generator;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.WriteConcern;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>. */
public final class CollectionManager implements Closeable {

  private final MongoClient client;
  private MongoDatabase db;
  private static final Logger LOG = Logger.getLogger(CollectionManager.class.getName());

  // TODO: a better way to manage db connection
  protected CollectionManager(MongoClient client, String dataBase) {
    this.client = client;
    if (dataBase != null && !dataBase.equals("")) {
      this.db = client.getDatabase(dataBase);
    } else {
      // Get the first available database - need to handle differently in new driver
      this.db = client.getDatabase("test"); // Default to 'test' database
    }
  }

  protected CollectionManager(MongoClient client, String dbName, String user, String password) {
    this(client, dbName);
    // Note: Authentication should be handled during MongoClient creation in newer drivers
    // The authenticate method is deprecated and removed in newer driver versions
  }

  protected CollectionManager(MongoClient client) {
    this.client = client;
  }

  /**
   * Uses the specified Database, creates one if it doesn't exist.
   *
   * @param dbName Database name
   */
  public void use(String dbName) {
    db = client.getDatabase(dbName);
  }

  /**
   * The number of documents in the specified collection.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @return the total of documents.
   */
  public <A extends Object> long count(Class<A> collectionClass) {
    return count(collectionClass, new MongoQuery());
  }

  /**
   * The number of documents that match the specified query.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @param query
   * @return the total of documents.
   */
  public <A extends Object> long count(Class<A> collectionClass, MongoQuery query) {
    long ret = 0l;
    try {
      A result = collectionClass.newInstance();
      String collectionName = reflectCollectionName(result);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      ret = collection.countDocuments(query.getQuery());
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return ret;
  }

  /**
   * Find all documents in the specified collection.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @return a list of documents.
   */
  public <A extends Object> List<A> find(Class<A> collectionClass) {
    return find(collectionClass, new MongoQuery());
  }

  /**
   * Find all documents that match the specified query in the given collection.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @param query
   * @return a list of documents.
   */
  public <A extends Object> List<A> find(Class<A> collectionClass, MongoQuery query) {
    List<A> resultSet = new ArrayList<>();
    try {
      A obj = collectionClass.newInstance();
      String collectionName = reflectCollectionName(obj);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      
      FindIterable<Document> findIterable = collection.find(query.getQuery());
      
      // Apply projection if specified
      if (query.getConstraints() != null) {
        findIterable = findIterable.projection(query.getConstraints());
      }
      
      // Apply ordering if specified  
      if (query.getOrderBy() != null) {
        findIterable = findIterable.sort(query.getOrderBy());
      }
      
      // Apply skip and limit
      if (query.getSkip() > 0) {
        findIterable = findIterable.skip(query.getSkip());
      }
      if (query.getLimit() > 0) {
        findIterable = findIterable.limit(query.getLimit());
      }
      
      for (Document document : findIterable) {
        loadObject(obj, document);
        resultSet.add(obj);
        obj = collectionClass.newInstance();
      }
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return resultSet;
  }

  /**
   * Find a single document of the specified collection.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @return a document.
   */
  public <A extends Object> A findOne(Class<A> collectionClass) {
    A result = null;
    try {
      result = collectionClass.newInstance();
      String collectionName = reflectCollectionName(result);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      Document doc = collection.find().first();
      if (doc == null) {
        return null;
      }
      loadObject(result, doc);
    } catch (NoSuchMethodException
        | SecurityException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | InstantiationException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return result;
  }

  /**
   * Find a single document that matches the specified query in the given collection.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @param query
   * @return a document.
   */
  public <A extends Object> A findOne(Class<A> collectionClass, MongoQuery query) {
    A result = null;
    try {
      result = collectionClass.newInstance();
      String collectionName = reflectCollectionName(result);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      
      FindIterable<Document> findIterable = collection.find(query.getQuery());
      if (query.getConstraints() != null) {
        findIterable = findIterable.projection(query.getConstraints());
      }
      
      Document doc = findIterable.first();
      if (doc == null) {
        return null;
      }
      loadObject(result, doc);
    } catch (NoSuchMethodException
        | SecurityException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | InstantiationException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return result;
  }

  /**
   * Find a single document that matches the specified id in the given collection.
   *
   * @param <A> generic type of the collection.
   * @param collectionClass
   * @param id
   * @return a document.
   */
  public <A extends Object> A findById(Class<A> collectionClass, String id) {
    return findOne(collectionClass, new MongoQuery("_id", id));
  }

  /**
   * Remove the specified document from the collection.
   *
   * @param document to be removed.
   */
  public void remove(Object document) {
    try {
      Document doc = loadDocument(document);
      String collectionName = reflectCollectionName(document);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      collection.deleteOne(doc);
    } catch (InstantiationException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException
        | SecurityException
        | IllegalArgumentException ex) {
      LOG.log(Level.SEVERE, "An error occured while removing this document: {0}", ex.getMessage());
    }
  }

  /**
   * Insert the document in a collection
   *
   * @param document
   * @return the <code>_id</code> of the inserted document, <code>null</code> if fails.
   */
  public String insert(Object document) {
    String _id = null;
    if (document == null) {
      return _id;
    }
    try {
      Document doc = loadDocument(document);
      String collectionName = reflectCollectionName(document);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      InsertOneResult result = collection.insertOne(doc);
      if (result.getInsertedId() != null) {
        _id = result.getInsertedId().asObjectId().getValue().toString();
      } else if (doc.containsKey("_id")) {
        _id = doc.get("_id").toString();
      }
      
      Field field = getFieldByAnnotation(document, com.arquivolivre.mongocom.annotations.ObjectId.class, false);
      if (field != null) {
        field.setAccessible(true);
        field.set(document, _id);
      }
      indexFields(document);
    } catch (InstantiationException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException
        | SecurityException
        | IllegalArgumentException
        | NoSuchFieldException ex) {
      LOG.log(Level.SEVERE, "An error occured while inserting this document: {0}", ex.getMessage());
    }
    if (_id != null) {
      LOG.log(Level.INFO, "Object \"{0}\" inserted successfully.", _id);
    }
    return _id;
  }

  public void update(MongoQuery query, Object document) {
    update(query, document, false, false);
  }

  public void update(MongoQuery query, Object document, boolean upsert, boolean multi) {
    update(query, document, upsert, multi, WriteConcern.ACKNOWLEDGED);
  }

  public void update(
      MongoQuery query, Object document, boolean upsert, boolean multi, WriteConcern concern) {
    try {
      Document doc = loadDocument(document);
      String collectionName = reflectCollectionName(document);
      MongoCollection<Document> collection = db.getCollection(collectionName).withWriteConcern(concern);
      
      if (multi) {
        collection.updateMany(query.getQuery(), new Document("$set", doc));
      } else {
        collection.updateOne(query.getQuery(), new Document("$set", doc));
      }
    } catch (InstantiationException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException
        | SecurityException
        | IllegalArgumentException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
  }

  public void updateMulti(MongoQuery query, Object document) {
    update(query, document, false, true);
  }

  public String save(Object document) {
    // TODO: a better way to throw/treat exceptions
    /*if (!document.getClass().isAnnotationPresent(Document.class)) {
    throw new NoSuchMongoCollectionException(document.getClass() + " is not a valid Document.");
    }*/
    String _id = null;
    if (document == null) {
      return _id;
    }
    try {
      Document doc = loadDocument(document);
      String collectionName = reflectCollectionName(document);
      MongoCollection<Document> collection = db.getCollection(collectionName);
      
      // If document has _id, use replaceOne with upsert, otherwise insertOne
      if (doc.containsKey("_id")) {
        collection.replaceOne(Filters.eq("_id", doc.get("_id")), doc, 
            new ReplaceOptions().upsert(true));
        _id = doc.get("_id").toString();
      } else {
        InsertOneResult result = collection.insertOne(doc);
        if (result.getInsertedId() != null) {
          _id = result.getInsertedId().asObjectId().getValue().toString();
        }
      }
      
      indexFields(document);
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException ex) {
      LOG.log(Level.SEVERE, "An error occured while saving this document: {0}", ex.getMessage());
    }
    if (_id != null) {
      LOG.log(Level.INFO, "Object \"{0}\" saved successfully.", _id);
    }
    return _id;
  }

  private void indexFields(Object document)
      throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException {
    String collectionName = reflectCollectionName(document);
    Field[] fields = getFieldsByAnnotation(document, Index.class);
    Map<String, List<String>> compoundIndexes = new TreeMap<>();
    IndexOptions compoundIndexesOpt = new IndexOptions().background(true);
    MongoCollection<Document> collection = db.getCollection(collectionName);
    for (Field field : fields) {
      Annotation annotation = field.getAnnotation(Index.class);
      IndexOptions options = new IndexOptions();
      Document indexKeys = new Document();
      String indexName = (String) annotation.annotationType().getMethod("value").invoke(annotation);
      String type = (String) annotation.annotationType().getMethod("type").invoke(annotation);
      boolean unique = (boolean) annotation.annotationType().getMethod("unique").invoke(annotation);
      boolean sparse = (boolean) annotation.annotationType().getMethod("sparse").invoke(annotation);
      boolean dropDups =
          (boolean) annotation.annotationType().getMethod("dropDups").invoke(annotation);
      boolean background =
          (boolean) annotation.annotationType().getMethod("background").invoke(annotation);
      int order = (int) annotation.annotationType().getMethod("order").invoke(annotation);
      if (!indexName.equals("")) {
        options.name(indexName);
      }
      options.background(background);
      options.unique(unique);
      options.sparse(sparse);
      // Note: dropDups is deprecated in newer MongoDB versions and not supported in driver 5.x
      String fieldName = field.getName();
      if (indexName.equals("") && type.equals("")) {
        indexKeys.append(fieldName, order);
        collection.createIndex(indexKeys, options);
      } else if (!indexName.equals("") && type.equals("")) {
        List<String> result = compoundIndexes.get(indexName);
        if (result == null) {
          result = new ArrayList<>();
          compoundIndexes.put(indexName, result);
        }
        result.add(fieldName + "_" + order);
      } else if (!type.equals("")) {
        indexKeys.append(fieldName, type);
        collection.createIndex(indexKeys, compoundIndexesOpt);
      }
    }
    Set<String> keys = compoundIndexes.keySet();
    for (String key : keys) {
      Document keysObj = new Document();
      IndexOptions namedOptions = new IndexOptions().background(true).name(key);
      for (String value : compoundIndexes.get(key)) {
        boolean with_ = false;
        if (value.startsWith("_")) {
          value = value.replaceFirst("_", "");
          with_ = true;
        }
        String[] opt = value.split("_");
        if (with_) {
          opt[0] = "_" + opt[0];
        }
        keysObj.append(opt[0], Integer.parseInt(opt[1]));
      }
      collection.createIndex(keysObj, namedOptions);
    }
  }

  private Document loadDocument(Object document)
      throws SecurityException, InstantiationException, InvocationTargetException,
          NoSuchMethodException {
    Field[] fields = document.getClass().getDeclaredFields();
    Document doc = new Document();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        String fieldName = field.getName();
        Object fieldContent = field.get(document);
        if (fieldContent == null && !field.isAnnotationPresent(GeneratedValue.class)) {
          continue;
        }
        if (fieldContent instanceof List list1) {
          List<Object> list = new ArrayList<>();
          boolean isInternal = field.isAnnotationPresent(Internal.class);
          for (Object item : list1) {
            if (isInternal) {
              list.add(loadDocument(item));
            } else {
              list.add(item);
            }
          }
          doc.append(fieldName, list);
        } else if (field.getType().isEnum()) {
          doc.append(fieldName, fieldContent.toString());
        } else if (field.isAnnotationPresent(Reference.class)) {
          doc.append(fieldName, new ObjectId(save(fieldContent)));
        } else if (field.isAnnotationPresent(Internal.class)) {
          doc.append(fieldName, loadDocument(fieldContent));
        } else if (field.isAnnotationPresent(Id.class) && !fieldContent.equals("")) {
          doc.append(fieldName, reflectId(field));
        } else if (field.isAnnotationPresent(GeneratedValue.class)) {
          Object value = reflectGeneratedValue(field, fieldContent);
          if (value != null) {
            doc.append(fieldName, value);
          }
        } else if (!field.isAnnotationPresent(com.arquivolivre.mongocom.annotations.ObjectId.class)) {
          doc.append(fieldName, fieldContent);
        } else if (!fieldContent.equals("")) {
          doc.append("_id", new ObjectId((String) fieldContent));
        }
      } catch (IllegalArgumentException | IllegalAccessException ex) {
        LOG.log(Level.SEVERE, null, ex);
      }
    }
    return doc;
  }

  private <A extends Object> void loadObject(A object, Document document)
      throws IllegalAccessException, IllegalArgumentException, SecurityException,
          InstantiationException {
    Field[] fields = object.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      String fieldName = field.getName();
      Object fieldContent = document.get(fieldName);
      if (fieldContent instanceof List list1) {
        Class<?> fieldArgClass = null;
        ParameterizedType genericFieldType = (ParameterizedType) field.getGenericType();
        Type[] fieldArgTypes = genericFieldType.getActualTypeArguments();
        for (Type fieldArgType : fieldArgTypes) {
          fieldArgClass = (Class<?>) fieldArgType;
        }
        List<Object> list = new ArrayList<>();
        boolean isInternal = field.isAnnotationPresent(Internal.class);
        for (Object item : list1) {
          if (isInternal) {
            Object o = fieldArgClass.newInstance();
            loadObject(o, (Document) item);
            list.add(o);
          } else {
            list.add(item);
          }
        }
        field.set(object, list);
      } else if ((fieldContent != null) && field.getType().isEnum()) {
        field.set(object, Enum.valueOf((Class) field.getType(), (String) fieldContent));
      } else if ((fieldContent != null) && field.isAnnotationPresent(Reference.class)) {
        field.set(
            object, findById(field.getType(), ((ObjectId) fieldContent).toString()));
      } else if (field.isAnnotationPresent(com.arquivolivre.mongocom.annotations.ObjectId.class)) {
        field.set(object, ((ObjectId) document.get("_id")).toString());
      } else if (field.getType().isPrimitive() && (fieldContent == null)) {
      } else if (fieldContent != null) {
        field.set(object, fieldContent);
      }
    }
  }

  private Field getFieldByAnnotation(
      Object obj, Class<? extends Annotation> annotationClass, boolean annotationRequired)
      throws NoSuchFieldException {
    Field[] fields = getFieldsByAnnotation(obj, annotationClass);
    if ((fields.length == 0) && annotationRequired) {
      throw new NoSuchFieldException("@" + annotationClass.getSimpleName() + " field not found.");
    } else if (fields.length > 0) {
      if (fields.length > 1) {
        LOG.log(
            Level.WARNING,
            "There are more than one @{0} field. Assuming the first one.",
            annotationClass.getSimpleName());
      }
      return fields[0];
    }
    return null;
  }

  private Field[] getFieldsByAnnotation(Object obj, Class<? extends Annotation> annotationClass) {
    Field[] fields = obj.getClass().getDeclaredFields();
    List<Field> fieldsAnnotated = new ArrayList<>();
    for (Field field : fields) {
      if (field.isAnnotationPresent(annotationClass)) {
        fieldsAnnotated.add(field);
      }
    }
    fields = new Field[fieldsAnnotated.size()];
    return fieldsAnnotated.toArray(fields);
  }

  private void invokeAnnotatedMethods(Object obj, Class<? extends Annotation> annotationClass) {
    Method[] methods = getMethodsByAnnotation(obj, annotationClass);
    for (Method method : methods) {
      try {
        method.invoke(obj);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        LOG.log(Level.SEVERE, null, ex);
      }
    }
  }

  private Method[] getMethodsByAnnotation(Object obj, Class<? extends Annotation> annotationClass) {
    Method[] methods = obj.getClass().getDeclaredMethods();
    List<Method> methodsAnnotated = new ArrayList<>();
    for (Method method : methods) {
      if (method.isAnnotationPresent(annotationClass)) {
        methodsAnnotated.add(method);
      }
    }
    return (Method[]) methodsAnnotated.toArray();
  }

  private String reflectCollectionName(Object document)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          SecurityException, IllegalArgumentException {
    Annotation annotation = document.getClass().getAnnotation(com.arquivolivre.mongocom.annotations.Document.class);
    String coll = (String) annotation.annotationType().getMethod("collection").invoke(annotation);
    if (coll.equals("")) {
      coll = document.getClass().getSimpleName();
    }
    return coll;
  }

  private <A extends Object> A reflectId(Field field)
      throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, InstantiationException {
    Annotation annotation = field.getAnnotation(Id.class);
    Boolean autoIncrement =
        (Boolean) annotation.annotationType().getMethod("autoIncrement").invoke(annotation);
    Class generator = (Class) annotation.annotationType().getMethod("generator").invoke(annotation);
    if (autoIncrement) {
      Generator g = (Generator) generator.newInstance();
      return g.generateValue(field.getDeclaringClass(), db);
    }
    return null;
  }

  private <A extends Object> A reflectGeneratedValue(Field field, Object oldValue)
      throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, InstantiationException {
    Annotation annotation = field.getAnnotation(GeneratedValue.class);
    Class<? extends Annotation> annotationType = annotation.annotationType();
    Boolean update = (Boolean) annotationType.getMethod("update").invoke(annotation);
    Class generator = (Class) annotationType.getMethod("generator").invoke(annotation);
    Generator g = (Generator) generator.newInstance();
    if ((update && (oldValue != null)) || (oldValue == null)) {
      return g.generateValue(field.getDeclaringClass(), db);
    } else if (oldValue instanceof Number) {

      boolean test = oldValue.equals(oldValue.getClass().cast(0));
      if (test) {
        return g.generateValue(field.getDeclaringClass(), db);
      } else if (update) {
        return g.generateValue(field.getDeclaringClass(), db);
      }
    }
    return null;
  }

  public String getStatus() {
    try {
      // Run a ping command to check connectivity
      Document ping = new Document("ping", 1);
      Document result = db.runCommand(ping);
      // Get database names as additional info
      List<String> dbNames = new ArrayList<>();
      for (String name : client.listDatabaseNames()) {
        dbNames.add(name);
      }
      return "MongoDB client connected. Ping result: " + result.toJson() + ". Databases: " + dbNames;
    } catch (Exception e) {
      return "MongoDB client connection error: " + e.getMessage();
    }
  }

  @Override
  public void close() {
    client.close();
  }
}
