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
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.InsertOneResult;
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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * This class is responsible for managing the connection to the MongoDB database and performing CRUD
 * operations on the collections.
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 */
public final class CollectionManager implements Closeable {

  private final MongoClient client;
  private MongoDatabase db;
  private static final Logger LOG = Logger.getLogger(CollectionManager.class.getName());

  // TODO: a better way to manage db connection
  protected CollectionManager(final MongoClient client, final String dataBase) {
    this.client = client;
    if (dataBase != null && !"".equals(dataBase)) {
      this.db = client.getDatabase(dataBase);
    } else {
      // Get the first available database - need to handle differently in new driver
      this.db = client.getDatabase("test"); // Default to 'test' database
    }
  }

  protected CollectionManager(
      final MongoClient client, final String dbName, final String user, final String password) {
    this(client, dbName);
    // Note: Authentication should be handled during MongoClient creation in newer drivers
    // The authenticate method is deprecated and removed in newer driver versions
  }

  protected CollectionManager(final MongoClient client) {
    this.client = client;
  }

  /**
   * Uses the specified Database, creates one if it doesn't exist.
   *
   * @param dbName Database name
   */
  public void use(final String dbName) {
    db = client.getDatabase(dbName);
  }

  /**
   * The number of documents in the specified collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @return the total of documents
   */
  public <A extends Object> long count(final Class<A> collectionClass) {
    return count(collectionClass, new MongoQuery());
  }

  /**
   * The number of documents that match the specified query.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param query the query to filter documents
   * @return the total of documents
   */
  public <A extends Object> long count(final Class<A> collectionClass, final MongoQuery query) {
    long ret = 0L;
    try {
      final A result = collectionClass.newInstance();
      final String collectionName = reflectCollectionName(result);
      final MongoCollection<Document> collection = db.getCollection(collectionName);
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
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @return a list of documents
   */
  public <A extends Object> List<A> find(final Class<A> collectionClass) {
    return find(collectionClass, new MongoQuery());
  }

  /**
   * Find all documents that match the specified query in the given collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param query the query to filter documents
   * @return a list of documents
   */
  public <A extends Object> List<A> find(final Class<A> collectionClass, final MongoQuery query) {
    final List<A> resultSet = new ArrayList<>();
    try {
      A obj = collectionClass.newInstance();
      final String collectionName = reflectCollectionName(obj);
      final MongoCollection<Document> collection = db.getCollection(collectionName);

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
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @return a document
   */
  public <A extends Object> A findOne(final Class<A> collectionClass) {
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
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param query the query to filter documents
   * @return a document
   */
  public <A extends Object> A findOne(final Class<A> collectionClass, final MongoQuery query) {
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
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param id the document ID
   * @return a document
   */
  public <A extends Object> A findById(final Class<A> collectionClass, final String id) {
    return findOne(collectionClass, new MongoQuery("_id", id));
  }

  /**
   * Remove the specified document from the collection.
   *
   * @param document to be removed.
   */
  public void remove(final Object document) {
    try {
      final Document doc = loadDocument(document);
      final String collectionName = reflectCollectionName(document);
      final MongoCollection<Document> collection = db.getCollection(collectionName);
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
   * Insert the document in a collection.
   *
   * @param document the document to insert
   * @return the <code>_id</code> of the inserted document, <code>null</code> if fails
   */
  public String insert(final Object document) {
    String id = null;
    if (document == null) {
      return id;
    }
    try {
      final Document doc = loadDocument(document);
      final String collectionName = reflectCollectionName(document);
      final MongoCollection<Document> collection = db.getCollection(collectionName);
      final InsertOneResult result = collection.insertOne(doc);
      if (result.getInsertedId() != null && result.getInsertedId().isObjectId()) {
        id = result.getInsertedId().asObjectId().getValue().toString();
      } else if (doc.containsKey("_id") && doc.get("_id") != null) {
        id = doc.get("_id").toString();
      }

      final Field field =
          getFieldByAnnotation(
              document, com.arquivolivre.mongocom.annotations.ObjectId.class, false);
      if (field != null) {
        field.setAccessible(true);
        field.set(document, id);
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
    if (id != null) {
      LOG.log(Level.INFO, "Object \"{0}\" inserted successfully.", id);
    }
    return id;
  }

  public void update(final MongoQuery query, final Object document) {
    update(query, document, false, false);
  }

  /**
   * Update documents matching the query.
   *
   * @param query the query to match documents
   * @param document the document with update values
   * @param upsert whether to insert if not found
   * @param multi whether to update multiple documents
   */
  public void update(
      final MongoQuery query, final Object document, final boolean upsert, final boolean multi) {
    update(query, document, upsert, multi, WriteConcern.ACKNOWLEDGED);
  }

  /**
   * Update documents matching the query with write concern.
   *
   * @param query the query to match documents
   * @param document the document with update values
   * @param upsert whether to insert if not found
   * @param multi whether to update multiple documents
   * @param concern the write concern to use
   */
  public void update(
      final MongoQuery query,
      final Object document,
      final boolean upsert,
      final boolean multi,
      final WriteConcern concern) {
    try {
      final Document doc = loadDocument(document);
      final String collectionName = reflectCollectionName(document);
      final MongoCollection<Document> collection =
          db.getCollection(collectionName).withWriteConcern(concern);

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

  public void updateMulti(final MongoQuery query, final Object document) {
    update(query, document, false, true);
  }

  /**
   * Save a document (insert or update).
   *
   * @param document the document to save
   * @return the document ID
   */
  public String save(final Object document) {
    // TODO: a better way to throw/treat exceptions
    /*if (!document.getClass().isAnnotationPresent(Document.class)) {
    throw new NoSuchMongoCollectionException(document.getClass() + " is not a valid Document.");
    }*/
    String id = null;
    if (document == null) {
      return id;
    }
    try {
      final Document doc = loadDocument(document);
      final String collectionName = reflectCollectionName(document);
      final MongoCollection<Document> collection = db.getCollection(collectionName);

      // If document has _id, use replaceOne with upsert, otherwise insertOne
      if (doc.containsKey("_id")) {
        collection.replaceOne(
            Filters.eq("_id", doc.get("_id")), doc, new ReplaceOptions().upsert(true));
        id = doc.get("_id").toString();
      } else {
        InsertOneResult result = collection.insertOne(doc);
        if (result.getInsertedId() != null && result.getInsertedId().isObjectId()) {
          id = result.getInsertedId().asObjectId().getValue().toString();
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
    if (id != null) {
      LOG.log(Level.INFO, "Object \"{0}\" saved successfully.", id);
    }
    return id;
  }

  private void indexFields(final Object document)
      throws NoSuchMethodException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException {
    final String collectionName = reflectCollectionName(document);
    final Field[] fields = getFieldsByAnnotation(document, Index.class);
    final Map<String, List<String>> compoundIndexes = new TreeMap<>();
    final IndexOptions compoundIndexesOpt = new IndexOptions().background(true);
    final MongoCollection<Document> collection = db.getCollection(collectionName);
    for (final Field field : fields) {
      final Annotation annotation = field.getAnnotation(Index.class);
      final IndexOptions options = new IndexOptions();
      final Document indexKeys = new Document();
      final String indexName =
          (String) annotation.annotationType().getMethod("value").invoke(annotation);
      final String type = (String) annotation.annotationType().getMethod("type").invoke(annotation);
      final boolean unique =
          (boolean) annotation.annotationType().getMethod("unique").invoke(annotation);
      final boolean sparse =
          (boolean) annotation.annotationType().getMethod("sparse").invoke(annotation);
      final boolean background =
          (boolean) annotation.annotationType().getMethod("background").invoke(annotation);
      final int order = (int) annotation.annotationType().getMethod("order").invoke(annotation);
      if (!"".equals(indexName)) {
        options.name(indexName);
      }
      options.background(background);
      options.unique(unique);
      options.sparse(sparse);
      // Note: dropDups is deprecated in newer MongoDB versions and not supported in driver 5.x
      final String fieldName = field.getName();
      if ("".equals(indexName) && "".equals(type)) {
        indexKeys.append(fieldName, order);
        collection.createIndex(indexKeys, options);
      } else if (!"".equals(indexName) && "".equals(type)) {
        List<String> result = compoundIndexes.get(indexName);
        if (result == null) {
          result = new ArrayList<>();
          compoundIndexes.put(indexName, result);
        }
        result.add(fieldName + '_' + order);
      } else if (!"".equals(type)) {
        indexKeys.append(fieldName, type);
        collection.createIndex(indexKeys, compoundIndexesOpt);
      }
    }
    for (final Map.Entry<String, List<String>> entry : compoundIndexes.entrySet()) {
      final String key = entry.getKey();
      final Document keysObj = new Document();
      final IndexOptions namedOptions = new IndexOptions().background(true).name(key);
      for (String value : entry.getValue()) {
        boolean withUnderscore = false;
        if (value.startsWith("_")) {
          value = value.replaceFirst("_", "");
          withUnderscore = true;
        }
        final String[] opt = value.split("_");
        if (withUnderscore) {
          opt[0] = "_" + opt[0];
        }
        keysObj.append(opt[0], Integer.parseInt(opt[1]));
      }
      collection.createIndex(keysObj, namedOptions);
    }
  }

  private Document loadDocument(final Object document)
      throws SecurityException,
          InstantiationException,
          InvocationTargetException,
          NoSuchMethodException {
    final Field[] fields = document.getClass().getDeclaredFields();
    final Document doc = new Document();
    for (final Field field : fields) {
      try {
        field.setAccessible(true);
        final String fieldName = field.getName();
        final Object fieldContent = field.get(document);
        if (fieldContent == null && !field.isAnnotationPresent(GeneratedValue.class)) {
          continue;
        }
        if (fieldContent instanceof List) {
          final List<Object> list = new ArrayList<>();
          final boolean isInternal = field.isAnnotationPresent(Internal.class);
          for (final Object item : (List) fieldContent) {
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
        } else if (field.isAnnotationPresent(Id.class) && !"".equals(fieldContent)) {
          doc.append(fieldName, reflectId(field));
        } else if (field.isAnnotationPresent(GeneratedValue.class)) {
          final Object value = reflectGeneratedValue(field, fieldContent);
          if (value != null) {
            doc.append(fieldName, value);
          }
        } else if (!field.isAnnotationPresent(
            com.arquivolivre.mongocom.annotations.ObjectId.class)) {
          doc.append(fieldName, fieldContent);
        } else if (!"".equals(fieldContent)) {
          doc.append("_id", new ObjectId((String) fieldContent));
        }
      } catch (IllegalArgumentException | IllegalAccessException ex) {
        LOG.log(Level.SEVERE, null, ex);
      }
    }
    return doc;
  }

  private <A extends Object> void loadObject(final A object, final Document document)
      throws IllegalAccessException,
          IllegalArgumentException,
          SecurityException,
          InstantiationException {
    final Field[] fields = object.getClass().getDeclaredFields();
    for (final Field field : fields) {
      field.setAccessible(true);
      final String fieldName = field.getName();
      final Object fieldContent = document.get(fieldName);
      if (fieldContent instanceof List) {
        Class<?> fieldArgClass = null;
        final ParameterizedType genericFieldType = (ParameterizedType) field.getGenericType();
        final Type[] fieldArgTypes = genericFieldType.getActualTypeArguments();
        for (final Type fieldArgType : fieldArgTypes) {
          fieldArgClass = (Class<?>) fieldArgType;
        }
        final List<Object> list = new ArrayList<>();
        final boolean isInternal = field.isAnnotationPresent(Internal.class);
        for (final Object item : (List) fieldContent) {
          if (isInternal) {
            final Object o = fieldArgClass.newInstance();
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
        field.set(object, findById(field.getType(), ((ObjectId) fieldContent).toString()));
      } else if (field.isAnnotationPresent(com.arquivolivre.mongocom.annotations.ObjectId.class)) {
        field.set(object, ((ObjectId) document.get("_id")).toString());
      } else if (field.getType().isPrimitive() && (fieldContent == null)) {
        // Skip setting primitive fields with null values
      } else if (fieldContent != null) {
        field.set(object, fieldContent);
      }
    }
  }

  private Field getFieldByAnnotation(
      final Object obj,
      final Class<? extends Annotation> annotationClass,
      final boolean annotationRequired)
      throws NoSuchFieldException {
    final Field[] fields = getFieldsByAnnotation(obj, annotationClass);
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

  private Field[] getFieldsByAnnotation(
      final Object obj, final Class<? extends Annotation> annotationClass) {
    Field[] fields = obj.getClass().getDeclaredFields();
    final List<Field> fieldsAnnotated = new ArrayList<>();
    for (final Field field : fields) {
      if (field.isAnnotationPresent(annotationClass)) {
        fieldsAnnotated.add(field);
      }
    }
    fields = new Field[fieldsAnnotated.size()];
    return fieldsAnnotated.toArray(fields);
  }

  private void invokeAnnotatedMethods(
      final Object obj, final Class<? extends Annotation> annotationClass) {
    final Method[] methods = getMethodsByAnnotation(obj, annotationClass);
    for (final Method method : methods) {
      try {
        method.invoke(obj);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        LOG.log(Level.SEVERE, null, ex);
      }
    }
  }

  private Method[] getMethodsByAnnotation(
      final Object obj, final Class<? extends Annotation> annotationClass) {
    final Method[] methods = obj.getClass().getDeclaredMethods();
    final List<Method> methodsAnnotated = new ArrayList<>();
    for (final Method method : methods) {
      if (method.isAnnotationPresent(annotationClass)) {
        methodsAnnotated.add(method);
      }
    }
    return methodsAnnotated.toArray(new Method[0]);
  }

  private String reflectCollectionName(final Object document)
      throws NoSuchMethodException,
          InvocationTargetException,
          IllegalAccessException,
          SecurityException,
          IllegalArgumentException {
    final Annotation annotation =
        document.getClass().getAnnotation(com.arquivolivre.mongocom.annotations.Document.class);
    String coll = (String) annotation.annotationType().getMethod("collection").invoke(annotation);
    if ("".equals(coll)) {
      coll = document.getClass().getSimpleName();
    }
    return coll;
  }

  private <A extends Object> A reflectId(final Field field)
      throws NoSuchMethodException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          InstantiationException {
    final Annotation annotation = field.getAnnotation(Id.class);
    final Boolean autoIncrement =
        (Boolean) annotation.annotationType().getMethod("autoIncrement").invoke(annotation);
    final Class generator =
        (Class) annotation.annotationType().getMethod("generator").invoke(annotation);
    if (autoIncrement) {
      final Generator g = (Generator) generator.newInstance();
      return g.generateValue(field.getDeclaringClass(), db);
    }
    return null;
  }

  private <A extends Object> A reflectGeneratedValue(final Field field, final Object oldValue)
      throws NoSuchMethodException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          InstantiationException {
    final Annotation annotation = field.getAnnotation(GeneratedValue.class);
    final Class<? extends Annotation> annotationType = annotation.annotationType();
    final Boolean update = (Boolean) annotationType.getMethod("update").invoke(annotation);
    final Class generator = (Class) annotationType.getMethod("generator").invoke(annotation);
    final Generator g = (Generator) generator.newInstance();
    if ((update && (oldValue != null)) || (oldValue == null)) {
      return g.generateValue(field.getDeclaringClass(), db);
    } else if (oldValue instanceof Number) {

      final boolean test = oldValue.equals(oldValue.getClass().cast(0));
      if (test) {
        return g.generateValue(field.getDeclaringClass(), db);
      } else if (update) {
        return g.generateValue(field.getDeclaringClass(), db);
      }
    }
    return null;
  }

  /**
   * Get the database connection status.
   *
   * @return status message
   */
  public String getStatus() {
    try {
      // Run a ping command to check connectivity
      final Document ping = new Document("ping", 1);
      final Document result = db.runCommand(ping);
      // Get database names as additional info
      final List<String> dbNames = new ArrayList<>();
      for (final String name : client.listDatabaseNames()) {
        dbNames.add(name);
      }
      return "MongoDB client connected. Ping result: "
          + result.toJson()
          + ". Databases: "
          + dbNames;
    } catch (Exception e) {
      return "MongoDB client connection error: " + e.getMessage();
    }
  }

  @Override
  public void close() {
    client.close();
  }
}
