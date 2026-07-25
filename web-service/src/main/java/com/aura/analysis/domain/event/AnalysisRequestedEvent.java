package com.aura.analysis.domain.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event published when an incident analysis job is requested.
 */
public record AnalysisRequestedEvent(
        String jobId,
        String incidentId,
        Instant occurredAt
) {
    public AnalysisRequestedEvent(String jobId, String incidentId) {
        this(jobId, incidentId, Instant.now());
    }

    public AnalysisRequestedEvent {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
