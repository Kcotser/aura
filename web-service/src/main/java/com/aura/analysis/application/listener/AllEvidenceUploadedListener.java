package com.aura.analysis.application.listener;

import com.aura.analysis.application.usecase.RequestAnalysisUseCase;
import com.aura.evidence.domain.event.AllEvidenceUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Spring Modulith event listener that automatically triggers AI analysis
 * when all 3 evidence streams for an incident are uploaded.
 */
@Component("analysisAllEvidenceUploadedListener")
public class AllEvidenceUploadedListener {

    private static final Logger log = LoggerFactory.getLogger(AllEvidenceUploadedListener.class);

    private final RequestAnalysisUseCase requestAnalysisUseCase;

    public AllEvidenceUploadedListener(RequestAnalysisUseCase requestAnalysisUseCase) {
        this.requestAnalysisUseCase = requestAnalysisUseCase;
    }

    @EventListener
    @ApplicationModuleListener
    public void onAllEvidenceUploaded(AllEvidenceUploadedEvent event) {
        log.info("Received AllEvidenceUploadedEvent for incidentId={}. Initiating automatic AI analysis.", event.incidentId());
        requestAnalysisUseCase.execute(event.incidentId());
    }
}
