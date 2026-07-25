package com.aura.evidence.application.dto;

/**
 * DTO representing a download reference for an evidence asset exposed across module boundaries.
 *
 * <p>Incluye {@code contentType} y {@code sizeBytes} porque el contexto de analisis necesita
 * decidir que archivos entran en el presupuesto inline del modelo multimodal antes de cargarlos
 * en memoria.
 */
public record EvidenceDownloadReference(
        String evidenceId,
        String incidentId,
        String mediaType,
        String gridFsFileId,
        String filename,
        String integrityHash,
        String contentType,
        long sizeBytes
) {
}
