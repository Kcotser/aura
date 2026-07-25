package com.aura.evidence.application.dto;

/**
 * DTO representing a download reference for an evidence asset exposed across module boundaries.
 */
public record EvidenceDownloadReference(
        String evidenceId,
        String incidentId,
        String mediaType,
        String gridFsFileId,
        String filename,
        String integrityHash
) {
}
