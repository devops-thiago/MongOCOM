/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.arquivolivre.mongocom.exceptions;

/**
 * Exception thrown when a MongoDB collection is not found.
 *
 * @author thiago
 */
public class NoSuchMongoCollectionException extends Exception {

  private static final long serialVersionUID = 1L;

  public NoSuchMongoCollectionException(final String message) {
    super(message);
  }
}
