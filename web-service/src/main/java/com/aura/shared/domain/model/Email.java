package com.aura.shared.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

/**
 * Value Object representing a validated email address.
 *
 * <p>Immutable and self-validating: construction fails with
 * {@link BusinessRuleViolationException} if the format is invalid.
 */
public record Email(String value) {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    /**
     * Compact canonical constructor that validates the email format.
     *
     * @param value the raw email string
     * @throws BusinessRuleViolationException if {@code value} is null, blank, or malformed
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("Email must not be blank");
        }
        String normalized = value.trim().toLowerCase();
        if (!normalized.matches(EMAIL_REGEX)) {
            throw new BusinessRuleViolationException("Invalid email format: " + value);
        }
        value = normalized;
    }

    /** Returns the canonical (lowercased, trimmed) email string. */
    public String value() {
        return value;
    }

    /** Factory method for readability at call sites. */
    public static Email of(String raw) {
        return new Email(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
