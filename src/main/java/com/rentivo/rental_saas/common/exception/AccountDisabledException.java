package com.rentivo.rental_saas.common.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException(String message) {
        super(message);
    }
}
