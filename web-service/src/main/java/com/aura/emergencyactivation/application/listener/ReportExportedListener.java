package com.aura.emergencyactivation.application.listener;

import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import com.aura.reporting.domain.event.ReportExportedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Spring Modulith event listener in Emergency Activation context that transitions Incident status to EXPORTED / CLOSED
 * when a user exports their incident report PDF.
 */
@Component("emergencyReportExportedListener")
public class ReportExportedListener {

    private static final Logger log = LoggerFactory.getLogger(ReportExportedListener.class);

    private final IncidentRepository incidentRepository;

    public ReportExportedListener(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @EventListener
    @ApplicationModuleListener
    public void onReportExported(ReportExportedEvent event) {
        log.info("EmergencyActivation received ReportExportedEvent for incidentId={}", event.incidentId());
        incidentRepository.findById(IncidentId.of(event.incidentId())).ifPresent(incident -> {
            if (incident.getStatus().canTransitionTo(IncidentStatus.EXPORTED)) {
                incident.export();
            }
            if (incident.getStatus().canTransitionTo(IncidentStatus.CLOSED)) {
                incident.close();
            }
            incidentRepository.save(incident);
        });
    }
}
