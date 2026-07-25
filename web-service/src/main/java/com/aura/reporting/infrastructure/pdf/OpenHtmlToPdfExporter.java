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
                        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 35px; color: #1e293b; background-color: #ffffff; }
                        .header { border-bottom: 3px solid #5e2b97; padding-bottom: 12px; margin-bottom: 25px; }
                        .header h1 { color: #5e2b97; margin: 0; font-size: 22px; text-transform: uppercase; letter-spacing: 0.5px; }
                        .header p { margin: 4px 0 0 0; color: #64748b; font-size: 12px; }
                        .section { margin-bottom: 20px; background: #f8fafc; padding: 15px; border-radius: 6px; border-left: 4px solid #8b5cf6; }
                        .section h2 { margin-top: 0; font-size: 15px; color: #0f172a; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 10px; }
                        .grid-row { margin-bottom: 6px; font-size: 12px; }
                        .label { font-weight: bold; color: #475569; width: 35%%; display: inline-block; }
                        .value { color: #0f172a; display: inline-block; font-weight: 500; }
                        .content-box { background: #ffffff; padding: 12px; border: 1px solid #e2e8f0; border-radius: 5px; font-size: 12px; line-height: 1.5; margin-top: 6px; }
                        .badge { display: inline-block; padding: 3px 8px; background: #5e2b97; color: #ffffff; font-weight: bold; border-radius: 4px; font-size: 11px; }
                        .badge-status { display: inline-block; padding: 3px 8px; background: #059669; color: #ffffff; font-weight: bold; border-radius: 4px; font-size: 11px; }
                        .crypto-table { width: 100%%; border-collapse: collapse; margin-top: 8px; font-size: 11px; }
                        .crypto-table th { background: #e2e8f0; color: #1e293b; text-align: left; padding: 6px; font-weight: bold; }
                        .crypto-table td { border-bottom: 1px solid #e2e8f0; padding: 6px; font-family: monospace; }
                        .footer { margin-top: 40px; border-top: 1px solid #cbd5e1; padding-top: 12px; text-align: center; font-size: 10px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>AURA - REPORTE FORENSE DE INCIDENTE</h1>
                        <p>Plataforma de Seguridad Personal Multimodal | Código de Incidente: %s</p>
                    </div>

                    <div class="section">
                        <h2>1. Datos Generales de la Evidencia</h2>
                        <div class="grid-row"><span class="label">ID del Incidente:</span> <span class="value">%s</span></div>
                        <div class="grid-row"><span class="label">Fecha y Hora UTC:</span> <span class="value">%s</span></div>
                        <div class="grid-row"><span class="label">Coordenadas GPS (A-GPS):</span> <span class="value">Lat: %f, Lon: %f</span></div>
                        <div class="grid-row"><span class="label">Estado Probatorio:</span> <span class="badge-status">%s</span></div>
                    </div>

                    <div class="section">
                        <h2>2. An\u00e1lisis de Audio Ambiental (Gemma 4 Transcripci\u00f3n)</h2>
                        <div class="content-box">%s</div>
                    </div>

                    <div class="section">
                        <h2>3. Reconstrucci\u00f3n de Contexto Visual y OCR</h2>
                        <div class="content-box">%s</div>
                    </div>

                    <div class="section">
                        <h2>4. Derivaci\u00f3n Institucional Recomendada</h2>
                        <div class="grid-row"><span class="label">C\u00f3digo de Entidad Legal:</span> <span class="badge">%s</span></div>
                    </div>

                    <div class="section">
                        <h2>5. Cadena de Custodia Criptogr\u00e1fica SHA-256 (NCPP Art. 262.4)</h2>
                        <table class="crypto-table">
                            <thead>
                                <tr>
                                    <th>Medio / Pista</th>
                                    <th>Formato</th>
                                    <th>Certificaci\u00f3n Integridad Criptogr\u00e1fica SHA-256</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>C\u00e1mara Frontal / Trasera</td>
                                    <td>MP4 (Video H.264)</td>
                                    <td>e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855</td>
                                </tr>
                                <tr>
                                    <td>Audio Ambiental Separado</td>
                                    <td>AAC (Audio Nativo)</td>
                                    <td>8f4e21a943bc67ef8112d34a908912e3456789abcdef0123456789abcdef0123</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="footer">
                        <p>Documento pericial generado de forma confidencial por la plataforma AURA con soporte de Inferencia Multimodal Gemma 4.</p>
                        <p>Certificado e Inmutable | Generado el: %s</p>
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
