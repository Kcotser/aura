package com.aura.reporting.application.usecase;

import com.aura.reporting.domain.event.ReportDraftedEvent;
import com.aura.reporting.domain.model.GeneralData;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.repository.IncidentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Use case to create an initial {@link IncidentReport} draft from AI analysis results.
 */
@Service
public class DraftReportUseCase {

    private static final Logger log = LoggerFactory.getLogger(DraftReportUseCase.class);

    private final IncidentReportRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DraftReportUseCase(IncidentReportRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public IncidentReport execute(
            String incidentId,
            String audioTranscriptSummary,
            String visualContextDescription,
            String suggestedEntityCode) {
        log.info("Drafting IncidentReport for incidentId={}", incidentId);

        IncidentReport report = repository.findByIncidentId(incidentId)
                .orElseGet(() -> IncidentReport.createDraft(
                        incidentId,
                        new GeneralData(Instant.now(), -12.046374, -77.042793),
                        audioTranscriptSummary,
                        visualContextDescription,
                        suggestedEntityCode
                ));

        IncidentReport saved = repository.save(report);
        eventPublisher.publishEvent(new ReportDraftedEvent(saved.getId().value(), incidentId));
        return saved;
    }
}
