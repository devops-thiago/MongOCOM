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

package com.arquivolivre.mongocom.repository;

import com.arquivolivre.mongocom.connection.MongoConnectionManager;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating and caching entity repositories.
 *
 * <p>This factory creates repository instances and caches them for reuse. Each entity class gets a
 * single repository instance that is shared across the application.
 *
 * <p><b>Design Pattern:</b> Factory - encapsulates repository creation logic
 *
 * <p><b>Design Pattern:</b> Singleton - one repository instance per entity class
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Uses ConcurrentHashMap for lock-free reads
 * after first access.
 *
 * <p><b>Performance:</b> First access per entity class creates repository (expensive). Subsequent
 * accesses are lock-free cache reads (fast).
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class RepositoryFactory {

  private static final Logger LOGGER = Logger.getLogger(RepositoryFactory.class.getName());

  private final MongoConnectionManager connectionManager;
  private final ConcurrentMap<Class<?>, EntityRepository<?, ?>> repositoryCache;

  /**
   * Creates a new repository factory.
   *
   * @param connectionManager the connection manager (must not be null)
   * @throws NullPointerException if connectionManager is null
   */
  public RepositoryFactory(final MongoConnectionManager connectionManager) {
    this.connectionManager =
        Objects.requireNonNull(connectionManager, "Connection manager cannot be null");
    this.repositoryCache = new ConcurrentHashMap<>();

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, "RepositoryFactory created");
    }
  }

  /**
   * Get repository for entity class (cached).
   *
   * <p>This method is thread-safe. ConcurrentHashMap.computeIfAbsent provides atomic check-and-set,
   * ensuring repository is created only once per class even under concurrent access.
   *
   * @param <T> the entity type
   * @param <I> the entity I type
   * @param entityClass the entity class (must not be null)
   * @return repository for the entity class (never null)
   * @throws NullPointerException if entityClass is null
   */
  @SuppressWarnings("unchecked")
  public <T, I> EntityRepository<T, I> getRepository(final Class<T> entityClass) {
    Objects.requireNonNull(entityClass, "Entity class cannot be null");

    return (EntityRepository<T, I>)
        repositoryCache.computeIfAbsent(entityClass, this::createRepository);
  }

  /**
   * Create a new repository instance.
   *
   * <p>This method is called once per entity class, then results are cached.
   *
   * @param entityClass the entity class
   * @return new repository instance
   */
  private <T> EntityRepository<T, ?> createRepository(final Class<T> entityClass) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, "Creating repository for: {0}", entityClass.getName());
    }

    final MongoEntityRepository<T, ?> repository =
        new MongoEntityRepository<>(entityClass, connectionManager);

    // Set the repository factory to enable reference resolution
    repository.setRepositoryFactory(this);

    return repository;
  }

  /**
   * Clear the repository cache.
   *
   * <p>This method is primarily useful for testing. In production, the cache should not be cleared
   * as it would force re-creation of repositories.
   */
  public void clearCache() {
    repositoryCache.clear();
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, "Repository cache cleared");
    }
  }

  /**
   * Get the current cache size.
   *
   * <p>Useful for monitoring and debugging.
   *
   * @return number of cached repositories
   */
  public int getCacheSize() {
    return repositoryCache.size();
  }

  /**
   * Check if repository is cached for a class.
   *
   * @param entityClass the entity class to check
   * @return true if repository is cached
   */
  public boolean isCached(final Class<?> entityClass) {
    return repositoryCache.containsKey(entityClass);
  }

  /**
   * Pre-load repositories for multiple entity classes.
   *
   * <p>This method can be called at application startup to pre-populate the cache, avoiding the
   * cost of lazy creation during first use.
   *
   * @param entityClasses classes to pre-load
   */
  @SafeVarargs
  public final void preloadRepositories(final Class<?>... entityClasses) {
    for (final Class<?> entityClass : entityClasses) {
      getRepository(entityClass);
    }
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.log(
          Level.INFO, "Pre-loaded repositories for {0} entity classes", entityClasses.length);
    }
  }

  @Override
  public String toString() {
    return "RepositoryFactory{" + "cachedRepositories=" + repositoryCache.size() + '}';
  }
}
