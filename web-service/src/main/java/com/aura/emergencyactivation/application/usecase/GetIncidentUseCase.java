package com.aura.emergencyactivation.application.usecase;

import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Use case to retrieve details of a specific incident.
 */
@Service
public class GetIncidentUseCase {

    private final IncidentRepository incidentRepository;

    public GetIncidentUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public Incident execute(String incidentIdStr, String userId) {
        IncidentId incidentId = IncidentId.of(incidentIdStr);
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentIdStr));

        if (!incident.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Incident", incidentIdStr);
        }

        return incident;
    }
}
