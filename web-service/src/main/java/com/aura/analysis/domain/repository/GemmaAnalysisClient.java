package com.aura.analysis.domain.repository;

import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.domain.model.EvidenceMediaPayload;
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

    /**
     * Performs multimodal AI analysis using real binary media payload streams (audio/video).
     *
     * @param incidentId the ID of the incident
     * @param evidenceFiles list of evidence asset references
     * @param mediaPayloads list of binary media content payloads
     * @return structured {@link AnalysisResult}
     */
    AnalysisResult analyzeIncident(String incidentId, List<EvidenceDownloadReference> evidenceFiles, List<EvidenceMediaPayload> mediaPayloads);
}

