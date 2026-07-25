package com.aura.evidence.domain.event;

import java.time.Instant;

/**
 * Domain event published when all 3 expected evidence streams
 * (FRONT_CAMERA, BACK_CAMERA, AMBIENT_AUDIO) have been uploaded for an incident.
 */
public record AllEvidenceUploadedEvent(
        String incidentId,
        Instant occurredAt
) {
    public AllEvidenceUploadedEvent(String incidentId) {
        this(incidentId, Instant.now());
    }
}
