package com.aura.analysis.domain.model;

/**
 * Structured result produced by the Gemma 4 multimodal AI analysis ACL.
 */
public record AnalysisResult(
        String audioTranscriptSummary,
        String visualContextDescription,
        String suggestedEntityCode,
        String threatLevel
) {

    public AnalysisResult {
        audioTranscriptSummary = audioTranscriptSummary != null ? audioTranscriptSummary : "";
        visualContextDescription = visualContextDescription != null ? visualContextDescription : "";
        suggestedEntityCode = suggestedEntityCode != null ? suggestedEntityCode : "comisaria-mujer";
        threatLevel = threatLevel != null ? threatLevel : "HIGH";
    }

    public static AnalysisResult empty() {
        return new AnalysisResult(
                "No audio transcript available",
                "No visual evidence processed",
                "comisaria-mujer",
                "MEDIUM"
        );
    }
}
