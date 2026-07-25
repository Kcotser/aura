package com.aura.emergencyactivation.domain.event;

import com.aura.emergencyactivation.domain.model.IncidentId;

import java.time.Instant;

/**
 * Event published when all evidence streams for an incident have been successfully uploaded.
 */
public record IncidentUploadedEvent(
        IncidentId incidentId,
        Instant occurredAt
) {
    public IncidentUploadedEvent(IncidentId incidentId) {
        this(incidentId, Instant.now());
    }
}
