package com.aura.evidence.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root for the Evidence bounded context.
 *
 * <p>Represents a single media asset stream (front camera video, back camera video,
 * or ambient audio) captured during an emergency incident.
 *
 * <p>Carries legal integrity SHA-256 hash and retention lifecycle metadata.
 * Persistence-ignorant: no Spring or Mongo annotations allowed here.
 */
public class EvidenceAsset {

    private final EvidenceAssetId id;
    private final String incidentId;
    private final MediaType type;
    private final StorageReference storageReference;
    private final IntegrityHash integrityHash;
    private final long sizeBytes;
    private final String contentType;
    private final Instant uploadedAt;
    private RetentionStatus retentionStatus;
    private Instant purgeAt;

    private EvidenceAsset(
            EvidenceAssetId id,
            String incidentId,
            MediaType type,
            StorageReference storageReference,
            IntegrityHash integrityHash,
            long sizeBytes,
            String contentType,
            Instant uploadedAt,
            RetentionStatus retentionStatus,
            Instant purgeAt
    ) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new BusinessRuleViolationException("Incident ID must not be blank");
        }
        if (sizeBytes <= 0) {
            throw new BusinessRuleViolationException("Size in bytes must be positive");
        }
        this.id = Objects.requireNonNull(id, "EvidenceAssetId must not be null");
        this.incidentId = incidentId.trim();
        this.type = Objects.requireNonNull(type, "MediaType must not be null");
        this.storageReference = Objects.requireNonNull(storageReference, "StorageReference must not be null");
        this.integrityHash = Objects.requireNonNull(integrityHash, "IntegrityHash must not be null");
        this.sizeBytes = sizeBytes;
        this.contentType = (contentType != null && !contentType.isBlank()) ? contentType.trim() : "application/octet-stream";
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt must not be null");
        this.retentionStatus = Objects.requireNonNull(retentionStatus, "RetentionStatus must not be null");
        this.purgeAt = purgeAt;
    }

    public static EvidenceAsset create(
            String incidentId,
            MediaType type,
            StorageReference storageReference,
            IntegrityHash integrityHash,
            long sizeBytes,
            String contentType
    ) {
        return new EvidenceAsset(
                EvidenceAssetId.generate(),
                incidentId,
                type,
                storageReference,
                integrityHash,
                sizeBytes,
                contentType,
                Instant.now(),
                RetentionStatus.ACTIVE,
                null
        );
    }

    public static EvidenceAsset reconstitute(
            EvidenceAssetId id,
            String incidentId,
            MediaType type,
            StorageReference storageReference,
            IntegrityHash integrityHash,
            long sizeBytes,
            String contentType,
            Instant uploadedAt,
            RetentionStatus retentionStatus,
            Instant purgeAt
    ) {
        return new EvidenceAsset(id, incidentId, type, storageReference, integrityHash, sizeBytes, contentType, uploadedAt, retentionStatus, purgeAt);
    }

    /**
     * Marks asset for immediate purge schedule.
     */
    public void schedulePurge() {
        if (this.retentionStatus == RetentionStatus.PURGED) {
            throw new BusinessRuleViolationException("Asset is already purged");
        }
        this.retentionStatus = RetentionStatus.PURGE_SCHEDULED;
        this.purgeAt = Instant.now();
    }

    /**
     * Marks asset as purged after physical deletion.
     */
    public void markPurged() {
        this.retentionStatus = RetentionStatus.PURGED;
    }

    // Getters

    public EvidenceAssetId getId() {
        return id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public MediaType getType() {
        return type;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public IntegrityHash getIntegrityHash() {
        return integrityHash;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public RetentionStatus getRetentionStatus() {
        return retentionStatus;
    }

    public Instant getPurgeAt() {
        return purgeAt;
    }
}
