package com.aura.reporting.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representation of {@link com.aura.reporting.domain.model.IncidentReport}.
 */
@Document(collection = "incident_reports")
public class IncidentReportDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String incidentId;

    private Instant timestamp;
    private Double latitude;
    private Double longitude;

    private String audioTranscriptSummary;
    private String visualContextDescription;
    private String suggestedEntityCode;
    private String status;

    private Instant createdAt;
    private Instant approvedAt;
    private Instant exportedAt;

    public IncidentReportDocument() {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(Instant exportedAt) {
        this.exportedAt = exportedAt;
    }
}
