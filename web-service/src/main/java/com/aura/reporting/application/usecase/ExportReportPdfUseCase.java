package com.aura.reporting.application.usecase;

import com.aura.reporting.domain.event.ReportExportedEvent;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.repository.IncidentReportRepository;
import com.aura.reporting.infrastructure.pdf.OpenHtmlToPdfExporter;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Use case to export an {@link IncidentReport} as a PDF binary.
 */
@Service
public class ExportReportPdfUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExportReportPdfUseCase.class);

    private final IncidentReportRepository repository;
    private final OpenHtmlToPdfExporter pdfExporter;
    private final ApplicationEventPublisher eventPublisher;

    public ExportReportPdfUseCase(
            IncidentReportRepository repository,
            OpenHtmlToPdfExporter pdfExporter,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.pdfExporter = pdfExporter;
        this.eventPublisher = eventPublisher;
    }

    public byte[] execute(String incidentId) {
        log.info("Exporting IncidentReport PDF for incidentId={}", incidentId);

        IncidentReport report = repository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", incidentId));

        report.markExported();
        repository.save(report);

        byte[] pdfBytes = pdfExporter.generatePdf(report);

        eventPublisher.publishEvent(new ReportExportedEvent(report.getId().value(), incidentId));
        return pdfBytes;
    }
}
