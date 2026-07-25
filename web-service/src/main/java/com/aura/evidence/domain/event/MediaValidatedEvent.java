package com.aura.evidence.domain.event;

import com.aura.evidence.domain.model.EvidenceAssetId;

import java.time.Instant;

public record MediaValidatedEvent(
        EvidenceAssetId assetId,
        String incidentId,
        String sha256Hash,
        Instant occurredAt
) {
    public MediaValidatedEvent(EvidenceAssetId assetId, String incidentId, String sha256Hash) {
        this(assetId, incidentId, sha256Hash, Instant.now());
    }
}
