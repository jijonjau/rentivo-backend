package com.rentivo.rental_saas.common.exception;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
