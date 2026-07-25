package com.aura.emergencyactivation.application.usecase;

import com.aura.emergencyactivation.domain.event.IncidentCancelledEvent;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Use case to cancel an incident (false alarm).
 */
@Service
public class CancelIncidentUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelIncidentUseCase.class);

    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CancelIncidentUseCase(IncidentRepository incidentRepository, ApplicationEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    public Incident execute(String incidentIdStr, String userId, String reason) {
        IncidentId incidentId = IncidentId.of(incidentIdStr);
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentIdStr));

        if (!incident.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Incident", incidentIdStr);
        }

        incident.cancel(reason);
        Incident saved = incidentRepository.save(incident);
        log.info("Incident cancelled: id={}, userId={}, reason={}", saved.getId(), userId, reason);

        eventPublisher.publishEvent(new IncidentCancelledEvent(saved.getId(), reason));

        return saved;
    }
}
