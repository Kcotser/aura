package com.aura.iam.domain.model;

import java.util.UUID;

/**
 * Typed identifier for the User aggregate.
 *
 * <p>Using a dedicated record instead of a raw {@code String} prevents
 * accidental mixing of different entity IDs at compile time.
 */
public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId must not be blank");
        }
    }

    /** Generates a new random UserId backed by UUID. */
    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
