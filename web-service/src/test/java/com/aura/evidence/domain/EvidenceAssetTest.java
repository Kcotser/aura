package com.aura.evidence.domain;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.IntegrityHash;
import com.aura.evidence.domain.model.MediaType;
import com.aura.evidence.domain.model.RetentionStatus;
import com.aura.evidence.domain.model.StorageReference;
import com.aura.shared.domain.exception.BusinessRuleViolationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EvidenceAsset Aggregate Unit Tests")
class EvidenceAssetTest {

    private final String incidentId = "inc-1001";
    private final StorageReference storageRef = new StorageReference("gridfs-id-123", "front_cam.mp4");
    private final IntegrityHash hash = new IntegrityHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

    @Test
    @DisplayName("Should create evidence asset in ACTIVE retention status")
    void shouldCreateEvidenceAsset() {
        EvidenceAsset asset = EvidenceAsset.create(
                incidentId,
                MediaType.FRONT_CAMERA,
                storageRef,
                hash,
                1024L,
                "video/mp4"
        );

        assertNotNull(asset.getId());
        assertEquals(incidentId, asset.getIncidentId());
        assertEquals(MediaType.FRONT_CAMERA, asset.getType());
        assertEquals(storageRef, asset.getStorageReference());
        assertEquals(hash, asset.getIntegrityHash());
        assertEquals(1024L, asset.getSizeBytes());
        assertEquals("video/mp4", asset.getContentType());
        assertEquals(RetentionStatus.ACTIVE, asset.getRetentionStatus());
        assertNull(asset.getPurgeAt());
    }

    @Test
    @DisplayName("Should schedule asset purge")
    void shouldSchedulePurge() {
        EvidenceAsset asset = EvidenceAsset.create(
                incidentId,
                MediaType.FRONT_CAMERA,
                storageRef,
                hash,
                1024L,
                "video/mp4"
        );

        asset.schedulePurge();

        assertEquals(RetentionStatus.PURGE_SCHEDULED, asset.getRetentionStatus());
        assertNotNull(asset.getPurgeAt());
    }

    @Test
    @DisplayName("Should validate SHA-256 length invariant")
    void shouldValidateSha256Length() {
        assertThrows(BusinessRuleViolationException.class, () -> new IntegrityHash("short-invalid-hash"));
    }
}
