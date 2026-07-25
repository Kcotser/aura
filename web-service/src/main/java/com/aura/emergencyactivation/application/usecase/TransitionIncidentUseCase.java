package com.aura.emergencyactivation.application.usecase;

import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Demo/hackathon shortcut use case to manually force a status transition on an incident.
 *
 * <p><strong>Note:</strong> This endpoint is intended strictly for demonstration and testing purposes
 * during the hackathon (e.g. simulating APPROVED status without waiting for the reporting module).
 */
@Service
public class TransitionIncidentUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransitionIncidentUseCase.class);

    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransitionIncidentUseCase(IncidentRepository incidentRepository, ApplicationEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    public Incident execute(String incidentIdStr, String userId, IncidentStatus targetStatus, String reason) {
        IncidentId incidentId = IncidentId.of(incidentIdStr);
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentIdStr));

        if (!incident.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Incident", incidentIdStr);
        }

        String transitionReason = (reason != null && !reason.isBlank()) ? reason : "Manual transition via demo endpoint";
        incident.transitionTo(targetStatus, transitionReason);

        Incident saved = incidentRepository.save(incident);
        log.info("Incident status manually transitioned: id={}, status={}, reason={}", saved.getId(), targetStatus, transitionReason);

        return saved;
    }
}
