package com.aura.reporting.interfaces.rest.response;

import com.aura.reporting.domain.model.IncidentReport;

import java.time.Instant;

public record IncidentReportResponse(
        String id,
        String incidentId,
        String status,
        String audioTranscriptSummary,
        String visualContextDescription,
        String suggestedEntityCode,
        Double latitude,
        Double longitude,
        Instant createdAt,
        Instant approvedAt,
        Instant exportedAt
) {
    public static IncidentReportResponse from(IncidentReport report) {
        return new IncidentReportResponse(
                report.getId().value(),
                report.getIncidentId(),
                report.getStatus().name(),
                report.getAudioTranscriptSummary(),
                report.getVisualContextDescription(),
                report.getSuggestedEntityCode(),
                report.getGeneralData() != null ? report.getGeneralData().latitude() : null,
                report.getGeneralData() != null ? report.getGeneralData().longitude() : null,
                report.getCreatedAt(),
                report.getApprovedAt(),
                report.getExportedAt()
        );
    }
}
