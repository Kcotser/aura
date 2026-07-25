package com.aura.iam.domain.model;

import java.util.UUID;

/**
 * Typed identifier for a DeviceSession entity.
 */
public record DeviceSessionId(String value) {

    public DeviceSessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DeviceSessionId must not be blank");
        }
    }

    public static DeviceSessionId generate() {
        return new DeviceSessionId(UUID.randomUUID().toString());
    }

    public static DeviceSessionId of(String value) {
        return new DeviceSessionId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
