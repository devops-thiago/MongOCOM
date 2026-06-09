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

import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import com.arquivolivre.mongocom.exception.MappingException;
import com.arquivolivre.mongocom.indexes.IndexManager;
import com.arquivolivre.mongocom.metadata.EntityMetadataExtractor;
import com.arquivolivre.mongocom.repository.EntityRepository;
import com.arquivolivre.mongocom.repository.RepositoryFactory;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facade for MongoDB collection management and CRUD operations.
 *
 * <p>This class provides a simplified interface for interacting with MongoDB collections. It
 * delegates to specialized components for different responsibilities:
 *
 * <ul>
 *   <li>Connection management - {@link MongoConnectionManager}
 *   <li>Metadata extraction - {@link EntityMetadataExtractor}
 *   <li>Repository operations - {@link RepositoryFactory}
 *   <li>Index management - {@link IndexManager}
 * </ul>
 *
 * <p><b>Design Pattern:</b> Facade - provides unified interface to subsystems
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. All delegated components are thread-safe.
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 * @author MongOCOM Team
 * @since 0.1
 */
public final class CollectionManager implements Closeable {

  private static final Logger LOG = Logger.getLogger(CollectionManager.class.getName());

  // Phase 3: Connection Management
  private final MongoConnectionManager connectionManager;

  // Phase 6: Repository Pattern
  private final RepositoryFactory repositoryFactory;

  // Phase 7: Index Management
  private final IndexManager indexManager;

  // Legacy support - kept for backward compatibility
  private final MongoClient client;
  private MongoDatabase db;

  /**
   * Creates a new collection manager with specified database.
   *
   * @param client the MongoDB client (must not be null)
   * @param dataBase the database name (if null or empty, uses "test")
   */
  protected CollectionManager(final MongoClient client, final String dataBase) {
    this.client = client;

    // Initialize connection manager
    final String dbName = (dataBase != null && !"".equals(dataBase)) ? dataBase : "test";
    this.connectionManager = MongoConnectionManager.getInstance(client, dbName);

    // Initialize repository factory
    this.repositoryFactory = new RepositoryFactory(connectionManager);

    // Initialize index manager
    this.indexManager = new IndexManager(connectionManager);

    // Keep legacy db reference for backward compatibility
    this.db = connectionManager.getDatabase(dbName);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "CollectionManager initialized for database: {0}", dbName);
    }
  }

  /**
   * Creates a new collection manager with authentication.
   *
   * <p>Note: Authentication should be handled during MongoClient creation in newer drivers.
   *
   * @param client the MongoDB client (must not be null)
   * @param dbName the database name
   * @param user the username (deprecated - handle in client creation)
   * @param password the password (deprecated - handle in client creation)
   */
  protected CollectionManager(
      final MongoClient client, final String dbName, final String user, final String password) {
    this(client, dbName);
    // Authentication is handled during MongoClient creation in newer drivers
  }

  /**
   * Creates a new collection manager without specifying database.
   *
   * @param client the MongoDB client (must not be null)
   */
  protected CollectionManager(final MongoClient client) {
    this(client, "test");
  }

  /**
   * Uses the specified Database, creates one if it doesn't exist.
   *
   * @param dbName Database name
   */
  public void use(final String dbName) {
    db = connectionManager.getDatabase(dbName);
  }

  /**
   * The number of documents in the specified collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @return the total of documents
   */
  public <A> long count(final Class<A> collectionClass) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    return repository.count();
  }

  /**
   * The number of documents that match the specified query.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param query the query to filter documents
   * @return the total of documents
   */
  public <A> long count(final Class<A> collectionClass, final MongoQuery query) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    return repository.count(query);
  }

  /**
   * Find all documents in the specified collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @return a list of documents
   */
  public <A> List<A> find(final Class<A> collectionClass) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    return repository.findAll();
  }

  /**
   * Find all documents that match the specified query in the given collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param query the query to filter documents
   * @return a list of documents
   */
  public <A> List<A> find(final Class<A> collectionClass, final MongoQuery query) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    return repository.find(query);
  }

  /**
   * Find a single document of the specified collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @return a document
   */
  public <A> A findOne(final Class<A> collectionClass) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    final Optional<A> result = repository.findOne(new MongoQuery());
    return result.orElse(null);
  }

  /**
   * Find a single document that matches the specified query in the given collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param query the query to filter documents
   * @return a document
   */
  public <A> A findOne(final Class<A> collectionClass, final MongoQuery query) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    final Optional<A> result = repository.findOne(query);
    return result.orElse(null);
  }

  /**
   * Find a single document that matches the specified id in the given collection.
   *
   * @param <A> generic type of the collection
   * @param collectionClass the class representing the collection
   * @param documentId the document ID
   * @return a document
   */
  public <A> A findById(final Class<A> collectionClass, final String documentId) {
    final EntityRepository<A, String> repository = repositoryFactory.getRepository(collectionClass);
    final Optional<A> result = repository.findById(documentId);
    return result.orElse(null);
  }

  /**
   * Remove the specified document from the collection.
   *
   * @param document to be removed.
   */
  public void remove(final Object document) {
    @SuppressWarnings({"unchecked", "rawtypes"})
    final EntityRepository repository = repositoryFactory.getRepository(document.getClass());
    repository.delete(document);
  }

  /**
   * Insert the document in a collection.
   *
   * @param document the document to insert
   * @return the <code>_id</code> of the inserted document, <code>null</code> if fails
   */
  public String insert(final Object document) {
    String result = null;
    if (document != null) {
      try {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final EntityRepository repository = repositoryFactory.getRepository(document.getClass());
        final Object insertedIdObj = repository.insert(document);
        final String insertedId = insertedIdObj != null ? insertedIdObj.toString() : null;
        indexManager.ensureIndexes(document.getClass());
        if (insertedId != null) {
          LOG.log(Level.INFO, "Object \"{0}\" inserted successfully.", insertedId);
        }
        result = insertedId;
      } catch (final MongoException ex) {
        LOG.log(Level.SEVERE, "MongoDB error while inserting document: {0}", ex.getMessage());
      } catch (final MappingException ex) {
        LOG.log(Level.SEVERE, "Mapping error while inserting document: {0}", ex.getMessage());
      } catch (final IllegalArgumentException ex) {
        LOG.log(Level.SEVERE, "Invalid document for insertion: {0}", ex.getMessage());
      }
    }
    return result;
  }

  /**
   * Update documents matching the query.
   *
   * @param query the query to match documents
   * @param document the document with update values
   */
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    final EntityRepository repository = repositoryFactory.getRepository(document.getClass());
    if (multi) {
      repository.updateMulti(query, document);
    } else {
      repository.update(query, document);
    }
  }

  /**
   * Update multiple documents matching the query.
   *
   * @param query the query to match documents
   * @param document the document with update values
   */
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
    String result = null;
    if (document != null) {
      try {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final EntityRepository repository = repositoryFactory.getRepository(document.getClass());
        final String savedId = (String) repository.save(document);
        indexManager.ensureIndexes(document.getClass());
        if (savedId != null) {
          LOG.log(Level.INFO, "Object \"{0}\" saved successfully.", savedId);
        }
        result = savedId;
      } catch (final MongoException ex) {
        LOG.log(Level.SEVERE, "MongoDB error while saving document: {0}", ex.getMessage());
      } catch (final MappingException ex) {
        LOG.log(Level.SEVERE, "Mapping error while saving document: {0}", ex.getMessage());
      } catch (final IllegalArgumentException ex) {
        LOG.log(Level.SEVERE, "Invalid document for save: {0}", ex.getMessage());
      }
    }
    return result;
  }

  /**
   * Get the database connection status.
   *
   * @return status message
   */
  public String getStatus() {
    return connectionManager.getStatus();
  }

  @Override
  public void close() {
    connectionManager.close();
  }
}
