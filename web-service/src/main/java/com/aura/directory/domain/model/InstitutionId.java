package com.aura.directory.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Type-safe identifier for the Institution aggregate.
 */
public class InstitutionId {
    private final String value;

    private InstitutionId(String value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static InstitutionId generate() {
        return new InstitutionId(UUID.randomUUID().toString());
    }

    public static InstitutionId of(String value) {
        return new InstitutionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionId that = (InstitutionId) o;
        return value.equals(that.value);
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
