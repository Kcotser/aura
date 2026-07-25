package com.aura.analysis.domain.event;

import com.aura.analysis.domain.model.AnalysisResult;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event published when an incident analysis job completes successfully.
 */
public record AnalysisCompletedEvent(
        String jobId,
        String incidentId,
        AnalysisResult result,
        Instant occurredAt
) {
    public AnalysisCompletedEvent(String jobId, String incidentId, AnalysisResult result) {
        this(jobId, incidentId, result, Instant.now());
    }

    public AnalysisCompletedEvent {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
