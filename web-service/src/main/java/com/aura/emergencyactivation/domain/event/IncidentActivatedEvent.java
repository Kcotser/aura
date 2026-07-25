package com.aura.emergencyactivation.domain.event;

import com.aura.emergencyactivation.domain.model.GpsLocation;
import com.aura.emergencyactivation.domain.model.IncidentId;

import java.time.Instant;

/**
 * Event published when a new safety incident is activated.
 */
public record IncidentActivatedEvent(
        IncidentId incidentId,
        String userId,
        GpsLocation gpsLocation,
        Instant occurredAt
) {
    public IncidentActivatedEvent(IncidentId incidentId, String userId, GpsLocation gpsLocation) {
        this(incidentId, userId, gpsLocation, Instant.now());
    }
}
