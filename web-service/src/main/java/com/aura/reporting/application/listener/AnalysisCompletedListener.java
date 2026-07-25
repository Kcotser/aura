package com.aura.reporting.application.listener;

import com.aura.analysis.domain.event.AnalysisCompletedEvent;
import com.aura.reporting.application.usecase.DraftReportUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Spring Modulith event listener that automatically drafts an {@link com.aura.reporting.domain.model.IncidentReport}
 * when AI analysis is completed.
 */
@Component("reportingAnalysisCompletedListener")
public class AnalysisCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(AnalysisCompletedListener.class);

    private final DraftReportUseCase draftReportUseCase;

    public AnalysisCompletedListener(DraftReportUseCase draftReportUseCase) {
        this.draftReportUseCase = draftReportUseCase;
    }

    @EventListener
    @ApplicationModuleListener
    public void onAnalysisCompleted(AnalysisCompletedEvent event) {
        log.info("Received AnalysisCompletedEvent for incidentId={}. Generating incident report draft.", event.incidentId());
        draftReportUseCase.execute(
                event.incidentId(),
                event.result().audioTranscriptSummary(),
                event.result().visualContextDescription(),
                event.result().suggestedEntityCode()
        );
    }
}
