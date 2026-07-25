package com.aura.reporting.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed Identifier for an {@link IncidentReport}.
 */
public record IncidentReportId(String value) {

    public IncidentReportId {
        Objects.requireNonNull(value, "IncidentReportId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("IncidentReportId value must not be blank");
        }
    }

    public static IncidentReportId generate() {
        return new IncidentReportId(UUID.randomUUID().toString());
    }

    public static IncidentReportId of(String id) {
        return new IncidentReportId(id);
    }
}
