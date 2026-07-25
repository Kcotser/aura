package com.aura.reporting.domain.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event published when an incident report draft is generated.
 */
public record ReportDraftedEvent(
        String reportId,
        String incidentId,
        Instant occurredAt
) {
    public ReportDraftedEvent(String reportId, String incidentId) {
        this(reportId, incidentId, Instant.now());
    }

    public ReportDraftedEvent {
        Objects.requireNonNull(reportId, "reportId must not be null");
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
