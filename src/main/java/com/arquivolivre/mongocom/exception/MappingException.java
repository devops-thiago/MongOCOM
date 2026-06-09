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

package com.arquivolivre.mongocom.exception;

/**
 * Exception thrown when object-document mapping fails.
 *
 * <p>This exception is thrown during serialization or deserialization when the mapping process
 * encounters an error that prevents successful conversion between Java objects and MongoDB
 * documents.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class MappingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new mapping exception with the specified detail message.
   *
   * @param message the detail message
   */
  public MappingException(final String message) {
    super(message);
  }

  /**
   * Constructs a new mapping exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  public MappingException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new mapping exception with the specified cause.
   *
   * @param cause the cause of this exception
   */
  public MappingException(final Throwable cause) {
    super(cause);
  }
}
