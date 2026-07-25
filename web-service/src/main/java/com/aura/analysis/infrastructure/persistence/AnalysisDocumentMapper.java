package com.aura.analysis.infrastructure.persistence;

import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.model.AnalysisJobId;
import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.domain.model.AnalysisStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Mapper between {@link AnalysisJob} domain aggregate and {@link AnalysisJobDocument}.
 */
@Component
public class AnalysisDocumentMapper {

    public AnalysisJobDocument toDocument(AnalysisJob domain) {
        AnalysisJobDocument doc = new AnalysisJobDocument();
        doc.setId(domain.getId().value());
        doc.setIncidentId(domain.getIncidentId());
        doc.setStatus(domain.getStatus().name());
        doc.setRequestedAt(domain.getRequestedAt());
        doc.setCompletedAt(domain.getCompletedAt());
        doc.setRawModelResponse(domain.getRawModelResponse());

        if (domain.getResult() != null) {
            doc.setAudioTranscriptSummary(domain.getResult().audioTranscriptSummary());
            doc.setVisualContextDescription(domain.getResult().visualContextDescription());
            doc.setSuggestedEntityCode(domain.getResult().suggestedEntityCode());
            doc.setThreatLevel(domain.getResult().threatLevel());
        }

        return doc;
    }

    public AnalysisJob toDomain(AnalysisJobDocument doc) {
        AnalysisJob job = new AnalysisJob(
                AnalysisJobId.of(doc.getId()),
                doc.getIncidentId(),
                doc.getRequestedAt()
        );

        if (doc.getStatus() != null) {
            setField(job, "status", AnalysisStatus.valueOf(doc.getStatus()));
        }
        if (doc.getCompletedAt() != null) {
            setField(job, "completedAt", doc.getCompletedAt());
        }
        if (doc.getRawModelResponse() != null) {
            setField(job, "rawModelResponse", doc.getRawModelResponse());
        }

        if (doc.getAudioTranscriptSummary() != null || doc.getSuggestedEntityCode() != null) {
            AnalysisResult result = new AnalysisResult(
                    doc.getAudioTranscriptSummary(),
                    doc.getVisualContextDescription(),
                    doc.getSuggestedEntityCode(),
                    doc.getThreatLevel()
            );
            setField(job, "result", result);
        }

        return job;
    }

    private void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map field " + fieldName + " on AnalysisJob", e);
        }
    }
}
