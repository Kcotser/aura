package com.aura.analysis.application.usecase;

import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.repository.AnalysisJobRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case to retry a failed analysis job.
 */
@Service
public class RetryAnalysisUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalysisUseCase.class);

    private final AnalysisJobRepository analysisJobRepository;
    private final RequestAnalysisUseCase requestAnalysisUseCase;

    public RetryAnalysisUseCase(AnalysisJobRepository analysisJobRepository, RequestAnalysisUseCase requestAnalysisUseCase) {
        this.analysisJobRepository = analysisJobRepository;
        this.requestAnalysisUseCase = requestAnalysisUseCase;
    }

    public AnalysisJob execute(String incidentId) {
        log.info("Retrying analysis job for incidentId={}", incidentId);

        AnalysisJob job = analysisJobRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisJob", incidentId));

        job.retry();
        analysisJobRepository.save(job);

        return requestAnalysisUseCase.execute(incidentId);
    }
}
