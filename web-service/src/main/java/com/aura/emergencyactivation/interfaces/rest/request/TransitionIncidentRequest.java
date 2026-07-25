package com.aura.emergencyactivation.interfaces.rest.request;

import com.aura.emergencyactivation.domain.model.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionIncidentRequest(
        @NotNull(message = "Target status is required")
        IncidentStatus targetStatus,
        String reason
) {}
