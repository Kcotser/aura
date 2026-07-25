package com.aura.analysis;

import com.aura.analysis.application.usecase.GetAnalysisJobUseCase;
import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.model.AnalysisStatus;
import com.aura.emergencyactivation.application.usecase.ActivateIncidentUseCase;
import com.aura.emergencyactivation.application.usecase.GetIncidentUseCase;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.evidence.application.usecase.UploadEvidenceUseCase;
import com.aura.reporting.application.usecase.ApproveReportUseCase;
import com.aura.reporting.application.usecase.EditReportUseCase;
import com.aura.reporting.application.usecase.ExportReportPdfUseCase;
import com.aura.reporting.application.usecase.GetReportUseCase;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.model.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@Tag("integration")
class AnalysisAndReportingIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private ActivateIncidentUseCase activateIncidentUseCase;

    @Autowired
    private UploadEvidenceUseCase uploadEvidenceUseCase;

    @Autowired
    private GetAnalysisJobUseCase getAnalysisJobUseCase;

    @Autowired
    private GetReportUseCase getReportUseCase;

    @Autowired
    private EditReportUseCase editReportUseCase;

    @Autowired
    private ApproveReportUseCase approveReportUseCase;

    @Autowired
    private ExportReportPdfUseCase exportReportPdfUseCase;

    @Autowired
    private GetIncidentUseCase getIncidentUseCase;

    @Test
    @DisplayName("Complete E2E Flow: Activate -> Upload 3 Assets -> Auto Analysis -> Auto Report -> Edit -> Approve -> Export PDF -> Closed Incident")
    void shouldExecuteFullAnalysisAndReportingPipeline() {
        String userId = "usr-full-flow-001";

        // 1. Activate Incident
        Incident incident = activateIncidentUseCase.execute(userId, -12.046374, -77.042793);
        String incidentId = incident.getId().getValue();
        assertEquals(IncidentStatus.ACTIVATED, incident.getStatus());

        // 2. Upload 3 evidence streams (AUDIO, FRONT_CAMERA, REAR_CAMERA) using MockMultipartFile
        MockMultipartFile audioFile = new MockMultipartFile("audio", "audio.mp4", "audio/mp4", "test audio bytes".getBytes());
        MockMultipartFile frontCameraFile = new MockMultipartFile("frontCamera", "front.mp4", "video/mp4", "test front video bytes".getBytes());
        MockMultipartFile rearCameraFile = new MockMultipartFile("rearCamera", "rear.mp4", "video/mp4", "test rear video bytes".getBytes());

        uploadEvidenceUseCase.execute(incidentId, audioFile, frontCameraFile, rearCameraFile);

        // 3. Verify automatic AI Analysis Job completion
        AnalysisJob analysisJob = getAnalysisJobUseCase.execute(incidentId);
        assertNotNull(analysisJob);
        assertEquals(AnalysisStatus.COMPLETED, analysisJob.getStatus());
        assertNotNull(analysisJob.getResult());

        // 4. Verify automatic Incident Report draft generation
        IncidentReport report = getReportUseCase.execute(incidentId);
        assertNotNull(report);
        assertEquals(ReportStatus.DRAFT, report.getStatus());

        // 5. Edit report draft
        IncidentReport editedReport = editReportUseCase.execute(
                incidentId,
                "Updated audio transcript summary",
                "Updated visual context description",
                "linea-100"
        );
        assertEquals(ReportStatus.EDITED, editedReport.getStatus());
        assertEquals("linea-100", editedReport.getSuggestedEntityCode());

        // 6. Approve report draft
        IncidentReport approvedReport = approveReportUseCase.execute(incidentId);
        assertEquals(ReportStatus.APPROVED, approvedReport.getStatus());

        // 7. Verify Incident transitioned to APPROVED
        Incident approvedIncident = getIncidentUseCase.execute(incidentId, userId);
        assertEquals(IncidentStatus.APPROVED, approvedIncident.getStatus());

        // 8. Export PDF
        byte[] pdfBytes = exportReportPdfUseCase.execute(incidentId);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // 9. Verify final Incident status is CLOSED
        Incident closedIncident = getIncidentUseCase.execute(incidentId, userId);
        assertEquals(IncidentStatus.CLOSED, closedIncident.getStatus());
    }
}
