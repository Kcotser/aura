package com.aura.reporting.application.usecase;

import com.aura.reporting.domain.event.ReportEditedEvent;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.repository.IncidentReportRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Use case to edit fields of an existing {@link IncidentReport} draft.
 */
@Service
public class EditReportUseCase {

    private static final Logger log = LoggerFactory.getLogger(EditReportUseCase.class);

    private final IncidentReportRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public EditReportUseCase(IncidentReportRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public IncidentReport execute(
            String incidentId,
            String audioTranscriptSummary,
            String visualContextDescription,
            String suggestedEntityCode) {
        log.info("Editing IncidentReport for incidentId={}", incidentId);

        IncidentReport report = repository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", incidentId));

        report.updateContent(audioTranscriptSummary, visualContextDescription, suggestedEntityCode);
        IncidentReport saved = repository.save(report);

        eventPublisher.publishEvent(new ReportEditedEvent(saved.getId().value(), incidentId));
        return saved;
    }
}
