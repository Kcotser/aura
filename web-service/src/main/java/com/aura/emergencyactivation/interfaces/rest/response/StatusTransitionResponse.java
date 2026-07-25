package com.aura.emergencyactivation.interfaces.rest.response;

import com.aura.emergencyactivation.domain.model.IncidentStatus;

import java.time.Instant;

public record StatusTransitionResponse(
        IncidentStatus fromStatus,
        IncidentStatus toStatus,
        Instant timestamp,
        String reason
) {}
