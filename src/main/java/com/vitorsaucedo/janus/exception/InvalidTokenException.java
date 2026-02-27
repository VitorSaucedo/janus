package com.vitorsaucedo.janus.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Token is invalid");
    }
}
