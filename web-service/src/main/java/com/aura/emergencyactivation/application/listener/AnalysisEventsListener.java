package com.aura.emergencyactivation.application.listener;

import com.aura.analysis.domain.event.AnalysisCompletedEvent;
import com.aura.analysis.domain.event.AnalysisRequestedEvent;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Spring Modulith event listener in Emergency Activation context that updates Incident state
 * when AI analysis events occur.
 */
@Component("emergencyAnalysisEventsListener")
public class AnalysisEventsListener {

    private static final Logger log = LoggerFactory.getLogger(AnalysisEventsListener.class);

    private final IncidentRepository incidentRepository;

    public AnalysisEventsListener(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @ApplicationModuleListener
    public void onAnalysisRequested(AnalysisRequestedEvent event) {
        log.info("EmergencyActivation received AnalysisRequestedEvent for incidentId={}", event.incidentId());
        incidentRepository.findById(IncidentId.of(event.incidentId())).ifPresent(incident -> {
            if (incident.getStatus().canTransitionTo(com.aura.emergencyactivation.domain.model.IncidentStatus.PROCESSING)) {
                incident.markProcessing();
                incidentRepository.save(incident);
            }
        });
    }

    @ApplicationModuleListener
    public void onAnalysisCompleted(AnalysisCompletedEvent event) {
        log.info("EmergencyActivation received AnalysisCompletedEvent for incidentId={}", event.incidentId());
        incidentRepository.findById(IncidentId.of(event.incidentId())).ifPresent(incident -> {
            if (incident.getStatus().canTransitionTo(com.aura.emergencyactivation.domain.model.IncidentStatus.DRAFT_READY)) {
                incident.markDraftReady();
                incidentRepository.save(incident);
            }
        });
    }
}
