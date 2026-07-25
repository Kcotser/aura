package com.aura.analysis.interfaces.rest.response;

import com.aura.analysis.domain.model.AnalysisJob;

import java.time.Instant;

public record AnalysisJobResponse(
        String id,
        String incidentId,
        String status,
        Instant requestedAt,
        Instant completedAt,
        AnalysisResultResponse result
) {
    public static AnalysisJobResponse from(AnalysisJob job) {
        return new AnalysisJobResponse(
                job.getId().value(),
                job.getIncidentId(),
                job.getStatus().name(),
                job.getRequestedAt(),
                job.getCompletedAt(),
                AnalysisResultResponse.from(job.getResult())
        );
    }
}
