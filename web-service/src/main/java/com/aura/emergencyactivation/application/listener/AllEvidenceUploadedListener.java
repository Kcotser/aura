package com.aura.emergencyactivation.application.listener;

import com.aura.emergencyactivation.domain.event.IncidentUploadedEvent;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import com.aura.evidence.domain.event.AllEvidenceUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Spring Modulith listener responding to {@link AllEvidenceUploadedEvent} from the evidence context.
 *
 * <p>Transitions the Incident status from RECORDING (or ACTIVATED) to UPLOADED automatically.
 */
@Component("emergencyAllEvidenceUploadedListener")
public class AllEvidenceUploadedListener {

    private static final Logger log = LoggerFactory.getLogger(AllEvidenceUploadedListener.class);

    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AllEvidenceUploadedListener(IncidentRepository incidentRepository, ApplicationEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @ApplicationModuleListener
    public void on(AllEvidenceUploadedEvent event) {
        log.info("Received AllEvidenceUploadedEvent for incidentId: {}", event.incidentId());
        IncidentId incidentId = IncidentId.of(event.incidentId());

        incidentRepository.findById(incidentId).ifPresent(incident -> {
            if (incident.getStatus().canTransitionTo(IncidentStatus.UPLOADED)) {
                incident.markUploaded();
                incidentRepository.save(incident);
                log.info("Incident transitioned to UPLOADED state: incidentId={}", incident.getId());
                eventPublisher.publishEvent(new IncidentUploadedEvent(incident.getId(), Instant.now()));
            } else {
                log.info("Skipping UPLOADED transition for incidentId={} as current status is {}", incident.getId(), incident.getStatus());
            }
        });
    }
}
