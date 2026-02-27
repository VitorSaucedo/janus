package com.vitorsaucedo.janus.exception;

public class PasswordRecentlyUsedException extends RuntimeException {
    public PasswordRecentlyUsedException() {
        super("Password used recently");
    }
}
