package com.aura.emergencyactivation.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object capturing a single status transition event for an Incident.
 * Preserves legal chain-of-custody audit logs.
 */
public record StatusTransition(
        IncidentStatus fromStatus,
        IncidentStatus toStatus,
        Instant timestamp,
        String reason
) {
    public StatusTransition {
        Objects.requireNonNull(toStatus, "Target status must not be null");
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public static StatusTransition initial(IncidentStatus initialStatus) {
        return new StatusTransition(null, initialStatus, Instant.now(), "Incident activated");
    }

    public static StatusTransition of(IncidentStatus from, IncidentStatus to, String reason) {
        return new StatusTransition(from, to, Instant.now(), reason);
    }
}
