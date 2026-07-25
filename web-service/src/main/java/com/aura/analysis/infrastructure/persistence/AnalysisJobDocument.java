package com.aura.analysis.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representation of {@link com.aura.analysis.domain.model.AnalysisJob}.
 */
@Document(collection = "analysis_jobs")
public class AnalysisJobDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String incidentId;

    private String status;
    private Instant requestedAt;
    private Instant completedAt;
    private String rawModelResponse;

    private String audioTranscriptSummary;
    private String visualContextDescription;
    private String suggestedEntityCode;
    private String threatLevel;

    public AnalysisJobDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getRawModelResponse() {
        return rawModelResponse;
    }

    public void setRawModelResponse(String rawModelResponse) {
        this.rawModelResponse = rawModelResponse;
    }

    public String getAudioTranscriptSummary() {
        return audioTranscriptSummary;
    }

    public void setAudioTranscriptSummary(String audioTranscriptSummary) {
        this.audioTranscriptSummary = audioTranscriptSummary;
    }

    public String getVisualContextDescription() {
        return visualContextDescription;
    }

    public void setVisualContextDescription(String visualContextDescription) {
        this.visualContextDescription = visualContextDescription;
    }

    public String getSuggestedEntityCode() {
        return suggestedEntityCode;
    }

    public void setSuggestedEntityCode(String suggestedEntityCode) {
        this.suggestedEntityCode = suggestedEntityCode;
    }

    public String getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }
}
