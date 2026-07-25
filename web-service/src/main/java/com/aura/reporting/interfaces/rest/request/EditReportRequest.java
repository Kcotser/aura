package com.aura.reporting.interfaces.rest.request;

public record EditReportRequest(
        String audioTranscriptSummary,
        String visualContextDescription,
        String suggestedEntityCode
) {
}
