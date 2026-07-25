package com.aura.shared.domain.exception;

/**
 * Thrown when a requested resource does not exist in the system.
 *
 * <p>Maps to HTTP 404 via {@link com.aura.shared.interfaces.rest.handler.GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(resourceName + " not found: " + identifier);
    }
}
