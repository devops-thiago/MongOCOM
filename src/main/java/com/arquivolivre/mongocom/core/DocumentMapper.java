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

package com.arquivolivre.mongocom.core;

import com.arquivolivre.mongocom.exception.MappingException;
import java.util.Map;

/**
 * Interface for mapping between Java objects and document representations.
 *
 * <p>This interface provides abstraction over concrete document implementations, allowing the
 * system to work with different document formats while maintaining loose coupling. Implementations
 * should be stateless and thread-safe.
 *
 * <p><b>Design Pattern:</b> Strategy Pattern - allows different mapping strategies to be used
 * interchangeably.
 *
 * <p><b>Thread Safety:</b> Implementations must be thread-safe as they may be shared across
 * multiple threads.
 *
 * @param <T> the type of entity to map
 * @author MongOCOM Team
 * @since 0.5
 */
public interface DocumentMapper<T> {

  /**
   * Convert an entity to a map representation.
   *
   * <p>This method serializes a Java object into a map structure that can be stored in a document
   * database. The map keys represent field names and values represent field values.
   *
   * @param entity the entity to convert (must not be null)
   * @return map representation of the entity (never null)
   * @throws MappingException if conversion fails
   * @throws IllegalArgumentException if entity is null
   */
  Map<String, Object> toMap(T entity);

  /**
   * Convert a map representation to an entity.
   *
   * <p>This method deserializes a map structure from a document database into a Java object. The
   * map keys represent field names and values represent field values.
   *
   * @param data the map representation (must not be null)
   * @param type the target entity class (must not be null)
   * @return entity instance populated with data from the map (never null)
   * @throws MappingException if conversion fails
   * @throws IllegalArgumentException if data or type is null
   */
  T fromMap(Map<String, Object> data, Class<T> type);
}
