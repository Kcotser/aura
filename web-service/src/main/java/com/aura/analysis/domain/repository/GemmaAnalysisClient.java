package com.aura.analysis.domain.repository;

import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.evidence.application.dto.EvidenceDownloadReference;

import java.util.List;

/**
 * ACL Port interface for Gemma 4 AI multimodal analysis.
 */
public interface GemmaAnalysisClient {

    /**
     * Performs multimodal AI analysis using evidence asset references for an incident.
     *
     * @param incidentId the ID of the incident
     * @param evidenceFiles list of evidence asset references
     * @return structured {@link AnalysisResult}
     */
    AnalysisResult analyzeIncident(String incidentId, List<EvidenceDownloadReference> evidenceFiles);
}
