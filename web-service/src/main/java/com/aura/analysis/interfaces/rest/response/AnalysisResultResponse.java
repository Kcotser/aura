package com.aura.analysis.interfaces.rest.response;

import com.aura.analysis.domain.model.AnalysisResult;

public record AnalysisResultResponse(
        String audioTranscriptSummary,
        String visualContextDescription,
        String suggestedEntityCode,
        String threatLevel
) {
    public static AnalysisResultResponse from(AnalysisResult result) {
        if (result == null) return null;
        return new AnalysisResultResponse(
                result.audioTranscriptSummary(),
                result.visualContextDescription(),
                result.suggestedEntityCode(),
                result.threatLevel()
        );
    }
}
