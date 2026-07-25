package com.aura.emergencyactivation.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of an {@link Incident}.
 */
public class IncidentId {

    private final String value;

    private IncidentId(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("Incident ID must not be blank");
        }
        this.value = value.trim();
    }

    public static IncidentId generate() {
        return new IncidentId(UUID.randomUUID().toString());
    }

    public static IncidentId of(String value) {
        return new IncidentId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentId that = (IncidentId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
