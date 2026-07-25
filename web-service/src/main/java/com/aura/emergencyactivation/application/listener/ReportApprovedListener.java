package com.aura.emergencyactivation.application.listener;

import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import com.aura.reporting.domain.event.ReportApprovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Spring Modulith event listener in Emergency Activation context that transitions Incident status to APPROVED
 * when a user approves their incident report draft.
 */
@Component("emergencyReportApprovedListener")
public class ReportApprovedListener {

    private static final Logger log = LoggerFactory.getLogger(ReportApprovedListener.class);

    private final IncidentRepository incidentRepository;

    public ReportApprovedListener(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @EventListener
    @ApplicationModuleListener
    public void onReportApproved(ReportApprovedEvent event) {
        log.info("EmergencyActivation received ReportApprovedEvent for incidentId={}", event.incidentId());
        incidentRepository.findById(IncidentId.of(event.incidentId())).ifPresent(incident -> {
            if (incident.getStatus().canTransitionTo(IncidentStatus.APPROVED)) {
                incident.approve();
                incidentRepository.save(incident);
            }
        });
    }
}
