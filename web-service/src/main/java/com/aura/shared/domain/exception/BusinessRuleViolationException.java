package com.aura.shared.domain.exception;

/**
 * Thrown when a business rule or domain invariant is violated.
 *
 * <p>Examples: invalid email format, password too short, PIN format mismatch.
 * Maps to HTTP 422 (Unprocessable Entity) via {@link com.aura.shared.interfaces.rest.handler.GlobalExceptionHandler}.
 */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
