package com.aura.analysis.application.usecase;

import com.aura.analysis.domain.event.AnalysisCompletedEvent;
import com.aura.analysis.domain.event.AnalysisFailedEvent;
import com.aura.analysis.domain.event.AnalysisRequestedEvent;
import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.domain.repository.AnalysisJobRepository;
import com.aura.analysis.domain.repository.GemmaAnalysisClient;
import com.aura.evidence.application.dto.EvidenceDownloadReference;
import com.aura.evidence.application.service.EvidenceQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case to request and execute multimodal AI analysis for an incident.
 */
@Service
public class RequestAnalysisUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestAnalysisUseCase.class);

    private final AnalysisJobRepository analysisJobRepository;
    private final GemmaAnalysisClient gemmaAnalysisClient;
    private final EvidenceQueryService evidenceQueryService;
    private final ApplicationEventPublisher eventPublisher;

    public RequestAnalysisUseCase(
            AnalysisJobRepository analysisJobRepository,
            GemmaAnalysisClient gemmaAnalysisClient,
            EvidenceQueryService evidenceQueryService,
            ApplicationEventPublisher eventPublisher) {
        this.analysisJobRepository = analysisJobRepository;
        this.gemmaAnalysisClient = gemmaAnalysisClient;
        this.evidenceQueryService = evidenceQueryService;
        this.eventPublisher = eventPublisher;
    }

    public AnalysisJob execute(String incidentId) {
        log.info("Executing RequestAnalysisUseCase for incidentId={}", incidentId);

        AnalysisJob job = analysisJobRepository.findByIncidentId(incidentId)
                .orElseGet(() -> AnalysisJob.create(incidentId));

        job.startProcessing();
        analysisJobRepository.save(job);

        eventPublisher.publishEvent(new AnalysisRequestedEvent(job.getId().value(), incidentId));

        try {
            List<EvidenceDownloadReference> evidenceFiles = evidenceQueryService.getDownloadReferencesForIncident(incidentId);
            AnalysisResult result = gemmaAnalysisClient.analyzeIncident(incidentId, evidenceFiles);

            job.complete(result, "{\"status\":\"SUCCESS\",\"incidentId\":\"" + incidentId + "\"}");
            analysisJobRepository.save(job);

            log.info("AnalysisJob completed successfully for incidentId={}, suggestedEntityCode={}", incidentId, result.suggestedEntityCode());
            eventPublisher.publishEvent(new AnalysisCompletedEvent(job.getId().value(), incidentId, result));

            return job;
        } catch (Exception e) {
            log.error("AnalysisJob failed for incidentId={}: {}", incidentId, e.getMessage(), e);
            job.fail("{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}");
            analysisJobRepository.save(job);

            eventPublisher.publishEvent(new AnalysisFailedEvent(job.getId().value(), incidentId, e.getMessage()));
            return job;
        }
    }
}
