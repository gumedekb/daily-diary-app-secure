package com.diary.io.exception;

/** Thrown when a requested resource does not exist; mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
