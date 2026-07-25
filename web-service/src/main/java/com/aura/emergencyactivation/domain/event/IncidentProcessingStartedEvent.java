package com.aura.emergencyactivation.domain.event;

import com.aura.emergencyactivation.domain.model.IncidentId;

import java.time.Instant;

public record IncidentProcessingStartedEvent(
        IncidentId incidentId,
        Instant occurredAt
) {
    public IncidentProcessingStartedEvent(IncidentId incidentId) {
        this(incidentId, Instant.now());
    }
}
