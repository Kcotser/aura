package com.aura.emergencyactivation.domain.event;

import com.aura.emergencyactivation.domain.model.IncidentId;

import java.time.Instant;

public record IncidentClosedEvent(
        IncidentId incidentId,
        Instant occurredAt
) {
    public IncidentClosedEvent(IncidentId incidentId) {
        this(incidentId, Instant.now());
    }
}
