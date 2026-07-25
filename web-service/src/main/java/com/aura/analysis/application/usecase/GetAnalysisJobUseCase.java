package com.aura.analysis.application.usecase;

import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.repository.AnalysisJobRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Use case to retrieve an analysis job by incidentId.
 */
@Service
public class GetAnalysisJobUseCase {

    private final AnalysisJobRepository analysisJobRepository;

    public GetAnalysisJobUseCase(AnalysisJobRepository analysisJobRepository) {
        this.analysisJobRepository = analysisJobRepository;
    }

    public AnalysisJob execute(String incidentId) {
        return analysisJobRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisJob", incidentId));
    }
}
