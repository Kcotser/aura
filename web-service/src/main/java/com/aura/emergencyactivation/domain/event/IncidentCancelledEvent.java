package com.aura.emergencyactivation.domain.event;

import com.aura.emergencyactivation.domain.model.IncidentId;

import java.time.Instant;

public record IncidentCancelledEvent(
        IncidentId incidentId,
        String reason,
        Instant occurredAt
) {
    public IncidentCancelledEvent(IncidentId incidentId, String reason) {
        this(incidentId, reason, Instant.now());
    }
}
