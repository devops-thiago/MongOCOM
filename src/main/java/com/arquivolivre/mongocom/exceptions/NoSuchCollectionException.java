/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arquivolivre.mongocom.exceptions;

/**
 *
 * @author thiago
 */
public class NoSuchCollectionException extends MongOCOMException {

    public NoSuchCollectionException(String message) {
        super(message);
    }

}
