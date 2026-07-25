package com.aura.reporting.application.usecase;

import com.aura.reporting.domain.event.ReportApprovedEvent;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.repository.IncidentReportRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Use case to approve an {@link IncidentReport}.
 */
@Service
public class ApproveReportUseCase {

    private static final Logger log = LoggerFactory.getLogger(ApproveReportUseCase.class);

    private final IncidentReportRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public ApproveReportUseCase(IncidentReportRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public IncidentReport execute(String incidentId) {
        log.info("Approving IncidentReport for incidentId={}", incidentId);

        IncidentReport report = repository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", incidentId));

        report.approve();
        IncidentReport saved = repository.save(report);

        eventPublisher.publishEvent(new ReportApprovedEvent(saved.getId().value(), incidentId));
        return saved;
    }
}
