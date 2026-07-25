package com.aura.shared.domain.exception;

/**
 * Base class for all domain-layer exceptions in Aura.
 *
 * <p>Domain exceptions represent violations of business invariants and rules.
 * They are unchecked (runtime) by convention: the domain should not be polluted
 * with checked exception declarations.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
