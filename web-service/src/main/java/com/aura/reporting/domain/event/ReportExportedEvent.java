package com.aura.reporting.domain.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event published when an incident report PDF is exported.
 */
public record ReportExportedEvent(
        String reportId,
        String incidentId,
        Instant occurredAt
) {
    public ReportExportedEvent(String reportId, String incidentId) {
        this(reportId, incidentId, Instant.now());
    }

    public ReportExportedEvent {
        Objects.requireNonNull(reportId, "reportId must not be null");
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
