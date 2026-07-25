package com.aura.analysis.domain.model;

/**
 * Aggregate Root for the Analysis context.
 *
 * <p>Represents a request to analyze a piece of evidence using Gemma 4.
 * The Anti-Corruption Layer translates Gemma 4's output into domain events.
 *
 * TODO: implement after hackathon
 */
public class AnalysisRequest {
    // TODO: implement after hackathon
    // Fields: id, evidenceItemId, incidentId, status, gemmaRequestId, resultSummary, analyzedAt
    // Methods: submit(), complete(AnalysisResult), fail(String reason)
}
