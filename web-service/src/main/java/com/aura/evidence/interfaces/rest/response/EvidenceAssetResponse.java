package com.aura.evidence.interfaces.rest.response;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.MediaType;
import com.aura.evidence.domain.model.RetentionStatus;

import java.time.Instant;

public record EvidenceAssetResponse(
        String id,
        String incidentId,
        MediaType type,
        String filename,
        String sha256Hash,
        long sizeBytes,
        String contentType,
        Instant uploadedAt,
        RetentionStatus retentionStatus,
        Instant purgeAt
) {
    public static EvidenceAssetResponse fromDomain(EvidenceAsset asset) {
        return new EvidenceAssetResponse(
                asset.getId().getValue(),
                asset.getIncidentId(),
                asset.getType(),
                asset.getStorageReference().getFilename(),
                asset.getIntegrityHash().getSha256(),
                asset.getSizeBytes(),
                asset.getContentType(),
                asset.getUploadedAt(),
                asset.getRetentionStatus(),
                asset.getPurgeAt()
        );
    }
}
