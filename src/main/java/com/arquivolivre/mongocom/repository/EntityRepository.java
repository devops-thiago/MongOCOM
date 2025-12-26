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

import com.arquivolivre.mongocom.management.MongoQuery;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for entity persistence operations.
 *
 * <p>This interface defines the contract for data access operations following the Repository
 * pattern. It provides type-safe CRUD operations and query capabilities.
 *
 * <p><b>Design Pattern:</b> Repository - abstracts data access logic
 *
 * <p><b>Benefits:</b>
 *
 * <ul>
 *   <li>Type safety with generics
 *   <li>Testable with mocks
 *   <li>Separation of concerns
 *   <li>Consistent API across entities
 *   <li>Easy to add caching, validation, etc.
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations must be thread-safe.
 *
 * @param <T> the entity type
 * @param <I> the entity ID type
 * @author MongOCOM Team
 * @since 0.5
 */
public interface EntityRepository<T, I> {

  /**
   * Count all entities of this type.
   *
   * @return the total count
   */
  long count();

  /**
   * Count entities matching the query.
   *
   * @param query the query criteria (must not be null)
   * @return the count of matching entities
   */
  long count(MongoQuery query);

  /**
   * Find all entities of this type.
   *
   * @return list of all entities (never null, may be empty)
   */
  List<T> findAll();

  /**
   * Find entities matching the query.
   *
   * @param query the query criteria (must not be null)
   * @return list of matching entities (never null, may be empty)
   */
  List<T> find(MongoQuery query);

  /**
   * Find first entity matching the query.
   *
   * @param query the query criteria (must not be null)
   * @return optional containing the entity if found, empty otherwise
   */
  Optional<T> findOne(MongoQuery query);

  /**
   * Find entity by ID.
   *
   * @param id the entity ID (must not be null)
   * @return optional containing the entity if found, empty otherwise
   */
  Optional<T> findById(I id);

  /**
   * Check if entity exists by ID.
   *
   * @param entityId the entity ID (must not be null)
   * @return true if entity exists, false otherwise
   */
  boolean existsById(I entityId);

  /**
   * Insert a new entity.
   *
   * <p>This method should fail if an entity with the same ID already exists.
   *
   * @param entity the entity to insert (must not be null)
   * @return the ID of the inserted entity
   */
  I insert(T entity);

  /**
   * Update an existing entity.
   *
   * <p>This method updates the entity matching the query with the provided entity data.
   *
   * @param query the query to find entity to update (must not be null)
   * @param entity the entity with updated data (must not be null)
   * @return the number of entities updated
   */
  long update(MongoQuery query, T entity);

  /**
   * Update multiple entities.
   *
   * @param query the query to find entities to update (must not be null)
   * @param entity the entity with updated data (must not be null)
   * @return the number of entities updated
   */
  long updateMulti(MongoQuery query, T entity);

  /**
   * Save an entity (insert or update).
   *
   * <p>If the entity has an ID and exists, it will be updated. Otherwise, it will be inserted.
   *
   * @param entity the entity to save (must not be null)
   * @return the ID of the saved entity
   */
  I save(T entity);

  /**
   * Delete an entity.
   *
   * @param entity the entity to delete (must not be null)
   * @return true if entity was deleted, false if not found
   */
  boolean delete(T entity);

  /**
   * Delete entities matching the query.
   *
   * @param query the query criteria (must not be null)
   * @return the number of entities deleted
   */
  long delete(MongoQuery query);

  /**
   * Delete entity by ID.
   *
   * @param id the entity ID (must not be null)
   * @return true if entity was deleted, false if not found
   */
  boolean deleteById(I id);

  /**
   * Delete all entities of this type.
   *
   * <p><b>Warning:</b> This operation cannot be undone!
   *
   * @return the number of entities deleted
   */
  long deleteAll();

  /**
   * Get the entity class managed by this repository.
   *
   * @return the entity class (never null)
   */
  Class<T> getEntityClass();

  /**
   * Get the collection name for this entity type.
   *
   * @return the collection name (never null)
   */
  String getCollectionName();
}
