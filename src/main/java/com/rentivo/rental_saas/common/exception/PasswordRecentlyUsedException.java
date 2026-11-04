package com.rentivo.rental_saas.common.exception;

public class PasswordRecentlyUsedException extends RuntimeException {
    public PasswordRecentlyUsedException(String message) {
        super(message);
    }
}