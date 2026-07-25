package com.aura.emergencyactivation.application.usecase;

import com.aura.emergencyactivation.domain.event.IncidentActivatedEvent;
import com.aura.emergencyactivation.domain.model.GpsLocation;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Use case to activate a new safety incident.
 */
@Service
public class ActivateIncidentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateIncidentUseCase.class);

    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivateIncidentUseCase(IncidentRepository incidentRepository, ApplicationEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    public Incident execute(String userId, double latitude, double longitude) {
        GpsLocation location = new GpsLocation(latitude, longitude);
        Incident incident = Incident.activate(userId, location);

        Incident saved = incidentRepository.save(incident);
        log.info("Incident activated: id={}, userId={}, location={}", saved.getId(), userId, location);

        eventPublisher.publishEvent(new IncidentActivatedEvent(saved.getId(), saved.getUserId(), saved.getGpsLocation()));

        return saved;
    }
}
