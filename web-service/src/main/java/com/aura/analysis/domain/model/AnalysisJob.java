package com.aura.analysis.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing an AI multimodal analysis job for an incident.
 */
public class AnalysisJob {

    private final AnalysisJobId id;
    private final String incidentId;
    private AnalysisStatus status;
    private final Instant requestedAt;
    private Instant completedAt;
    private String rawModelResponse;
    private AnalysisResult result;

    public AnalysisJob(AnalysisJobId id, String incidentId, Instant requestedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.incidentId = Objects.requireNonNull(incidentId, "incidentId must not be null");
        this.status = AnalysisStatus.REQUESTED;
        this.requestedAt = requestedAt != null ? requestedAt : Instant.now();
    }

    public static AnalysisJob create(String incidentId) {
        return new AnalysisJob(AnalysisJobId.generate(), incidentId, Instant.now());
    }

    public void startProcessing() {
        if (this.status != AnalysisStatus.REQUESTED && this.status != AnalysisStatus.FAILED) {
            throw new IllegalStateException("Cannot start processing job in status: " + status);
        }
        this.status = AnalysisStatus.IN_PROGRESS;
    }

    public void complete(AnalysisResult result, String rawModelResponse) {
        this.status = AnalysisStatus.COMPLETED;
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.rawModelResponse = rawModelResponse;
        this.completedAt = Instant.now();
    }

    public void fail(String rawModelResponse) {
        this.status = AnalysisStatus.FAILED;
        this.rawModelResponse = rawModelResponse;
        this.completedAt = Instant.now();
    }

    public void retry() {
        if (this.status != AnalysisStatus.FAILED) {
            throw new IllegalStateException("Only FAILED jobs can be retried, current status: " + status);
        }
        this.status = AnalysisStatus.REQUESTED;
    }

    public AnalysisJobId getId() {
        return id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getRawModelResponse() {
        return rawModelResponse;
    }

    public AnalysisResult getResult() {
        return result;
    }
}
