package com.aura.emergencyactivation.domain.event;

import com.aura.emergencyactivation.domain.model.IncidentId;

import java.time.Instant;

public record IncidentApprovedEvent(
        IncidentId incidentId,
        Instant occurredAt
) {
    public IncidentApprovedEvent(IncidentId incidentId) {
        this(incidentId, Instant.now());
    }
}
