package com.aura.reporting;

import com.aura.reporting.domain.model.GeneralData;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.infrastructure.pdf.OpenHtmlToPdfExporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenHtmlToPdfExporterTest {

    @Test
    @DisplayName("should generate valid non-empty PDF binary from IncidentReport")
    void shouldGeneratePdfBinary() {
        OpenHtmlToPdfExporter pdfExporter = new OpenHtmlToPdfExporter();

        IncidentReport report = IncidentReport.createDraft(
                "inc-test-123",
                new GeneralData(Instant.now(), -12.046, -77.042),
                "Distress audio stream captured",
                "Dark room visual footage",
                "linea-100"
        );

        byte[] pdfBytes = pdfExporter.generatePdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Verify standard PDF header magic bytes %PDF-
        String pdfHeader = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", pdfHeader);
    }
}
