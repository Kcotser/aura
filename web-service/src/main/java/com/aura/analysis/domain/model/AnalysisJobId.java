package com.aura.analysis.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed Identifier for an {@link AnalysisJob}.
 */
public record AnalysisJobId(String value) {

    public AnalysisJobId {
        Objects.requireNonNull(value, "AnalysisJobId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AnalysisJobId value must not be blank");
        }
    }

    public static AnalysisJobId generate() {
        return new AnalysisJobId(UUID.randomUUID().toString());
    }

    public static AnalysisJobId of(String id) {
        return new AnalysisJobId(id);
    }
}
