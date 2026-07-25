package com.aura.reporting.infrastructure.persistence;

import com.aura.reporting.domain.model.GeneralData;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.model.IncidentReportId;
import com.aura.reporting.domain.model.ReportStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Mapper between {@link IncidentReport} domain aggregate and {@link IncidentReportDocument}.
 */
@Component
public class ReportDocumentMapper {

    public IncidentReportDocument toDocument(IncidentReport domain) {
        IncidentReportDocument doc = new IncidentReportDocument();
        doc.setId(domain.getId().value());
        doc.setIncidentId(domain.getIncidentId());
        doc.setStatus(domain.getStatus().name());

        if (domain.getGeneralData() != null) {
            doc.setTimestamp(domain.getGeneralData().timestamp());
            doc.setLatitude(domain.getGeneralData().latitude());
            doc.setLongitude(domain.getGeneralData().longitude());
        }

        doc.setAudioTranscriptSummary(domain.getAudioTranscriptSummary());
        doc.setVisualContextDescription(domain.getVisualContextDescription());
        doc.setSuggestedEntityCode(domain.getSuggestedEntityCode());

        doc.setCreatedAt(domain.getCreatedAt());
        doc.setApprovedAt(domain.getApprovedAt());
        doc.setExportedAt(domain.getExportedAt());

        return doc;
    }

    public IncidentReport toDomain(IncidentReportDocument doc) {
        GeneralData generalData = new GeneralData(
                doc.getTimestamp(),
                doc.getLatitude(),
                doc.getLongitude()
        );

        IncidentReport report = new IncidentReport(
                IncidentReportId.of(doc.getId()),
                doc.getIncidentId(),
                generalData,
                doc.getAudioTranscriptSummary(),
                doc.getVisualContextDescription(),
                doc.getSuggestedEntityCode()
        );

        if (doc.getStatus() != null) {
            setField(report, "status", ReportStatus.valueOf(doc.getStatus()));
        }
        if (doc.getApprovedAt() != null) {
            setField(report, "approvedAt", doc.getApprovedAt());
        }
        if (doc.getExportedAt() != null) {
            setField(report, "exportedAt", doc.getExportedAt());
        }
        if (doc.getCreatedAt() != null) {
            setField(report, "createdAt", doc.getCreatedAt());
        }

        return report;
    }

    private void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map field " + fieldName + " on IncidentReport", e);
        }
    }
}
