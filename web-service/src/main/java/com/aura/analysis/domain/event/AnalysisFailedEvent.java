package com.aura.analysis.domain.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event published when an incident analysis job fails.
 */
public record AnalysisFailedEvent(
        String jobId,
        String incidentId,
        String reason,
        Instant occurredAt
) {
    public AnalysisFailedEvent(String jobId, String incidentId, String reason) {
        this(jobId, incidentId, reason, Instant.now());
    }

    public AnalysisFailedEvent {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
