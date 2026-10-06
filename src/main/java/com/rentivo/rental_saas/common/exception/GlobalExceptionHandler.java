package com.rentivo.rental_saas.common.exception;

import com.rentivo.rental_saas.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<?> handleAccountDisabledException(AccountDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<?> handleInvalidOtpException(InvalidOtpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
}