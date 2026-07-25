package com.aura.shared.domain.exception;

/**
 * Thrown when authentication fails (invalid credentials, expired token, etc.).
 *
 * <p>Maps to HTTP 401 via {@link com.aura.shared.interfaces.rest.handler.GlobalExceptionHandler}.
 */
public class AuthenticationFailedException extends DomainException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
