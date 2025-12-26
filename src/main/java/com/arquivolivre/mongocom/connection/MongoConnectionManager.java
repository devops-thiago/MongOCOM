/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
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

package com.arquivolivre.mongocom.connection;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 * Thread-safe singleton connection manager for MongoDB.
 *
 * <p>This class manages MongoDB client connections and database instances using the Singleton
 * pattern with double-checked locking. Database connections are cached in a ConcurrentHashMap for
 * lock-free reads after first access.
 *
 * <p><b>Design Pattern:</b> Singleton - ensures only one connection manager instance exists.
 *
 * <p><b>Thread Safety Guarantees:</b>
 *
 * <ul>
 *   <li>Singleton instance: volatile field + synchronized initialization (double-checked locking)
 *   <li>Database cache: ConcurrentHashMap provides lock-free reads and atomic updates
 *   <li>All fields: final (immutable after construction)
 * </ul>
 *
 * <p><b>Performance:</b>
 *
 * <ul>
 *   <li>First getInstance() call: synchronized (slow, but only once)
 *   <li>Subsequent getInstance() calls: volatile read (fast)
 *   <li>First getDatabase() call per database: atomic cache update
 *   <li>Subsequent getDatabase() calls: lock-free cache read (very fast)
 * </ul>
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class MongoConnectionManager implements Closeable {

  private static final Logger LOG = Logger.getLogger(MongoConnectionManager.class.getName());

  // Singleton instance with volatile for thread-safe lazy initialization
  private static volatile MongoConnectionManager instance;

  // Lock object for synchronized block (separate from instance for clarity)
  private static final Object LOCK = new Object();

  // All instance fields are final - immutable after construction
  private final MongoClient client;
  private final ConcurrentMap<String, MongoDatabase> databaseCache;
  private final String defaultDatabaseName;

  /**
   * Private constructor for singleton pattern.
   *
   * <p>Initializes the connection manager with a MongoDB client and optional default database. If a
   * default database is specified, it is pre-cached for immediate availability.
   *
   * @param client MongoDB client (must not be null)
   * @param defaultDatabaseName default database name (may be null)
   * @throws NullPointerException if client is null
   */
  private MongoConnectionManager(final MongoClient client, final String defaultDatabaseName) {
    this.client = Objects.requireNonNull(client, "MongoClient cannot be null");
    this.defaultDatabaseName = defaultDatabaseName;
    this.databaseCache = new ConcurrentHashMap<>();

    // Pre-cache default database if specified
    if (defaultDatabaseName != null && !defaultDatabaseName.isEmpty()) {
      this.databaseCache.put(defaultDatabaseName, client.getDatabase(defaultDatabaseName));
      LOG.log(Level.INFO, "Initialized with default database: {0}", defaultDatabaseName);
    }
  }

  /**
   * Get singleton instance (thread-safe).
   *
   * <p>Uses double-checked locking pattern for thread-safe lazy initialization. The volatile
   * keyword ensures visibility of the instance across threads.
   *
   * <p><b>Thread Safety:</b> First call is synchronized (slow), subsequent calls are volatile reads
   * (fast).
   *
   * @param client MongoDB client (must not be null)
   * @param defaultDatabaseName default database name (may be null)
   * @return singleton instance (never null)
   * @throws NullPointerException if client is null
   */
  public static MongoConnectionManager getInstance(
      final MongoClient client, final String defaultDatabaseName) {
    // First check (no locking) - fast path for already initialized instance
    if (instance == null) {
      // Synchronize only if instance is null
      synchronized (LOCK) {
        // Second check (with locking) - ensure only one thread creates instance
        if (instance == null) {
          instance = new MongoConnectionManager(client, defaultDatabaseName);
          LOG.log(Level.INFO, "MongoConnectionManager singleton instance created");
        }
      }
    }
    return instance;
  }

  /**
   * Get singleton instance with existing configuration.
   *
   * <p>This method should only be called after the instance has been initialized with {@link
   * #getInstance(MongoClient, String)}.
   *
   * @return singleton instance (never null)
   * @throws IllegalStateException if instance has not been initialized
   */
  public static MongoConnectionManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException(
          "MongoConnectionManager not initialized. "
              + "Call getInstance(MongoClient, String) first.");
    }
    return instance;
  }

  /**
   * Get database by name (thread-safe).
   *
   * <p>Uses ConcurrentHashMap.computeIfAbsent for atomic cache operations. First access per
   * database performs initialization, subsequent accesses are lock-free reads from cache.
   *
   * <p><b>Thread Safety:</b> ConcurrentHashMap.computeIfAbsent provides atomic check-and-set
   * semantics.
   *
   * @param dbName database name (must not be null)
   * @return MongoDB database (never null)
   * @throws NullPointerException if dbName is null
   */
  public MongoDatabase getDatabase(final String dbName) {
    Objects.requireNonNull(dbName, "Database name cannot be null");

    return databaseCache.computeIfAbsent(
        dbName,
        name -> {
          LOG.log(Level.FINE, "Creating database connection: {0}", name);
          return client.getDatabase(name);
        });
  }

  /**
   * Get default database (thread-safe).
   *
   * <p>Returns the database specified during initialization. If no default database was specified,
   * throws IllegalStateException.
   *
   * @return default MongoDB database (never null)
   * @throws IllegalStateException if no default database configured
   */
  public MongoDatabase getDefaultDatabase() {
    if (defaultDatabaseName == null || defaultDatabaseName.isEmpty()) {
      throw new IllegalStateException(
          "No default database configured. Use getDatabase(String) instead.");
    }
    return getDatabase(defaultDatabaseName);
  }

  /**
   * Get connection status.
   *
   * <p>Performs a ping command to verify connectivity and lists available databases.
   *
   * @return status message with ping result and available databases
   */
  public String getStatus() {
    String status;
    try {
      // Use default database if available, otherwise use admin database
      final MongoDatabase database =
          defaultDatabaseName != null ? getDefaultDatabase() : client.getDatabase("admin");

      // Run ping command to check connectivity
      final Document ping = new Document("ping", 1);
      final Document result = database.runCommand(ping);

      // Get database names
      final List<String> dbNames = new ArrayList<>();
      for (final String name : client.listDatabaseNames()) {
        dbNames.add(name);
      }

      status =
          String.format(
              "MongoDB client connected. Ping result: %s. Databases: %s. Cached: %d",
              result.toJson(), dbNames, databaseCache.size());

    } catch (final MongoException e) {
      LOG.log(Level.SEVERE, "MongoDB connection status check failed", e);
      status = "MongoDB client connection error: " + e.getMessage();
    } catch (final IllegalStateException e) {
      LOG.log(Level.SEVERE, "MongoDB client not properly initialized", e);
      status = "MongoDB client initialization error: " + e.getMessage();
    }
    return status;
  }

  /**
   * Get MongoDB client.
   *
   * <p>Provides access to the underlying MongoDB client for advanced operations.
   *
   * @return MongoDB client (never null)
   */
  public MongoClient getClient() {
    return client;
  }

  /**
   * Get default database name.
   *
   * @return default database name (may be null if not configured)
   */
  public String getDefaultDatabaseName() {
    return defaultDatabaseName;
  }

  /**
   * Get number of cached databases.
   *
   * <p>Useful for monitoring and debugging.
   *
   * @return cache size
   */
  public int getCacheSize() {
    return databaseCache.size();
  }

  /**
   * Check if a database is cached.
   *
   * @param dbName database name to check
   * @return true if database is cached
   */
  public boolean isCached(final String dbName) {
    return databaseCache.containsKey(dbName);
  }

  /**
   * Clear database cache (useful for testing).
   *
   * <p>Does not close database connections, only clears the cache. Subsequent calls to
   * getDatabase() will re-create connections.
   *
   * <p><b>Warning:</b> This method should only be used in testing. In production, the cache should
   * not be cleared.
   */
  public void clearCache() {
    databaseCache.clear();
    LOG.log(Level.INFO, "Database cache cleared");
  }

  /**
   * Close MongoDB client and clear cache.
   *
   * <p>Closes the underlying MongoDB client connection and clears the database cache. After calling
   * this method, the connection manager should not be used.
   */
  @Override
  public void close() {
    try {
      client.close();
      databaseCache.clear();
      LOG.log(Level.INFO, "MongoDB connection closed");
    } catch (final Exception e) {
      LOG.log(Level.SEVERE, "Error closing MongoDB connection", e);
    }
  }

  /**
   * Reset singleton instance (for testing only).
   *
   * <p><b>Warning:</b> This method is intended for testing only. It closes the current instance and
   * resets the singleton, allowing a new instance to be created.
   *
   * <p>This method is package-private to prevent misuse in production code.
   */
  static void resetInstance() {
    synchronized (LOCK) {
      if (instance != null) {
        instance.close();
        instance = null;
        LOG.log(Level.INFO, "MongoConnectionManager singleton instance reset");
      }
    }
  }
}
