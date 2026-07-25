package com.aura.reporting.domain.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event published when an incident report is edited.
 */
public record ReportEditedEvent(
        String reportId,
        String incidentId,
        Instant occurredAt
) {
    public ReportEditedEvent(String reportId, String incidentId) {
        this(reportId, incidentId, Instant.now());
    }

    public ReportEditedEvent {
        Objects.requireNonNull(reportId, "reportId must not be null");
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
