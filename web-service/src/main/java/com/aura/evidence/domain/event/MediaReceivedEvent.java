package com.aura.evidence.domain.event;

import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.model.MediaType;

import java.time.Instant;

public record MediaReceivedEvent(
        EvidenceAssetId assetId,
        String incidentId,
        MediaType type,
        Instant occurredAt
) {
    public MediaReceivedEvent(EvidenceAssetId assetId, String incidentId, MediaType type) {
        this(assetId, incidentId, type, Instant.now());
    }
}
