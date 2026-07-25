package com.aura.reporting.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root representing an Incident Legal Report draft & final document.
 */
public class IncidentReport {

    private final IncidentReportId id;
    private final String incidentId;
    private GeneralData generalData;
    private String audioTranscriptSummary;
    private String visualContextDescription;
    private String suggestedEntityCode;
    private ReportStatus status;
    private final Instant createdAt;
    private Instant approvedAt;
    private Instant exportedAt;

    public IncidentReport(
            IncidentReportId id,
            String incidentId,
            GeneralData generalData,
            String audioTranscriptSummary,
            String visualContextDescription,
            String suggestedEntityCode) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.incidentId = Objects.requireNonNull(incidentId, "incidentId must not be null");
        this.generalData = generalData != null ? generalData : new GeneralData(Instant.now(), 0.0, 0.0);
        this.audioTranscriptSummary = audioTranscriptSummary != null ? audioTranscriptSummary : "";
        this.visualContextDescription = visualContextDescription != null ? visualContextDescription : "";
        this.suggestedEntityCode = suggestedEntityCode != null ? suggestedEntityCode : "comisaria-mujer";
        this.status = ReportStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public static IncidentReport createDraft(
            String incidentId,
            GeneralData generalData,
            String audioTranscriptSummary,
            String visualContextDescription,
            String suggestedEntityCode) {
        return new IncidentReport(
                IncidentReportId.generate(),
                incidentId,
                generalData,
                audioTranscriptSummary,
                visualContextDescription,
                suggestedEntityCode
        );
    }

    public void updateContent(String audioTranscriptSummary, String visualContextDescription, String suggestedEntityCode) {
        if (status == ReportStatus.APPROVED || status == ReportStatus.EXPORTED) {
            throw new IllegalStateException("Cannot edit report once APPROVED or EXPORTED");
        }
        if (audioTranscriptSummary != null) this.audioTranscriptSummary = audioTranscriptSummary;
        if (visualContextDescription != null) this.visualContextDescription = visualContextDescription;
        if (suggestedEntityCode != null) this.suggestedEntityCode = suggestedEntityCode;
        this.status = ReportStatus.EDITED;
    }

    public void approve() {
        if (status == ReportStatus.APPROVED || status == ReportStatus.EXPORTED) {
            return;
        }
        this.status = ReportStatus.APPROVED;
        this.approvedAt = Instant.now();
    }

    public void markExported() {
        this.status = ReportStatus.EXPORTED;
        this.exportedAt = Instant.now();
    }

    public IncidentReportId getId() {
        return id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public GeneralData getGeneralData() {
        return generalData;
    }

    public String getAudioTranscriptSummary() {
        return audioTranscriptSummary;
    }

    public String getVisualContextDescription() {
        return visualContextDescription;
    }

    public String getSuggestedEntityCode() {
        return suggestedEntityCode;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getExportedAt() {
        return exportedAt;
    }
}
