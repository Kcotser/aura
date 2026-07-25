package com.aura.reporting.infrastructure.pdf;

import com.aura.reporting.domain.model.IncidentReport;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * PDF Exporter using OpenHTMLtoPDF.
 * Renders an HTML template with incident report data into a PDF byte array.
 */
@Component
public class OpenHtmlToPdfExporter {

    private static final Logger log = LoggerFactory.getLogger(OpenHtmlToPdfExporter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneId.of("UTC"));

    public byte[] generatePdf(IncidentReport report) {
        log.info("Generating PDF report for incidentId={}", report.getIncidentId());

        String htmlTemplate = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8" />
                    <title>Reporte de Incidente - Aura</title>
                    <style>
                        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 40px; color: #1e293b; background-color: #ffffff; }
                        .header { border-bottom: 2px solid #ef4444; padding-bottom: 15px; margin-bottom: 30px; }
                        .header h1 { color: #dc2626; margin: 0; font-size: 24px; text-transform: uppercase; }
                        .header p { margin: 5px 0 0 0; color: #64748b; font-size: 13px; }
                        .section { margin-bottom: 25px; background: #f8fafc; padding: 18px; border-radius: 8px; border-left: 4px solid #3b82f6; }
                        .section h2 { margin-top: 0; font-size: 16px; color: #0f172a; text-transform: uppercase; letter-spacing: 0.5px; }
                        .grid { width: 100%%; margin-bottom: 10px; }
                        .label { font-weight: bold; color: #475569; width: 30%%; display: inline-block; font-size: 13px; }
                        .value { color: #0f172a; display: inline-block; font-size: 13px; }
                        .content-box { background: #ffffff; padding: 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 13px; line-height: 1.5; margin-top: 8px; }
                        .badge { display: inline-block; padding: 4px 8px; background: #dbeafe; color: #1e40af; font-weight: bold; border-radius: 4px; font-size: 12px; }
                        .footer { margin-top: 50px; border-top: 1px solid #e2e8f0; padding-top: 15px; text-align: center; font-size: 11px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>Aura - Reporte de Incidente de Seguridad</h1>
                        <p>Plataforma Multimodal de Seguridad Personal | Código de Incidente: %s</p>
                    </div>

                    <div class="section">
                        <h2>Información General</h2>
                        <div><span class="label">ID de Incidente:</span> <span class="value">%s</span></div>
                        <div><span class="label">Fecha y Hora:</span> <span class="value">%s</span></div>
                        <div><span class="label">Ubicación GPS:</span> <span class="value">Lat: %f, Lon: %f</span></div>
                        <div><span class="label">Estado del Reporte:</span> <span class="badge">%s</span></div>
                    </div>

                    <div class="section">
                        <h2>Análisis de Audio Ambiental</h2>
                        <div class="content-box">%s</div>
                    </div>

                    <div class="section">
                        <h2>Análisis de Contexto Visual</h2>
                        <div class="content-box">%s</div>
                    </div>

                    <div class="section">
                        <h2>Entidad Sugerida para Derivación Legal</h2>
                        <div><span class="label">Código de Entidad:</span> <span class="badge">%s</span></div>
                    </div>

                    <div class="footer">
                        <p>Este documento es un reporte generado automáticamente por la plataforma Aura con soporte de IA (Gemma 4).</p>
                        <p>Generado el: %s</p>
                    </div>
                </body>
                </html>
                """.formatted(
                report.getIncidentId(),
                report.getIncidentId(),
                DATE_FORMATTER.format(report.getGeneralData().timestamp()),
                report.getGeneralData().latitude(),
                report.getGeneralData().longitude(),
                report.getStatus().name(),
                escapeHtml(report.getAudioTranscriptSummary()),
                escapeHtml(report.getVisualContextDescription()),
                escapeHtml(report.getSuggestedEntityCode()),
                DATE_FORMATTER.format(report.getCreatedAt())
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlTemplate, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to render PDF using OpenHTMLtoPDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
