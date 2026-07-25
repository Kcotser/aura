package com.aura.evidence.domain.event;

import com.aura.evidence.domain.model.EvidenceAssetId;

import java.time.Instant;

public record MediaPurgedEvent(
        EvidenceAssetId assetId,
        String incidentId,
        Instant occurredAt
) {
    public MediaPurgedEvent(EvidenceAssetId assetId, String incidentId) {
        this(assetId, incidentId, Instant.now());
    }
}
