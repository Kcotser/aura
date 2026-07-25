package com.aura.evidence.infrastructure.persistence;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.model.IntegrityHash;
import com.aura.evidence.domain.model.StorageReference;
import org.springframework.stereotype.Component;

@Component
public class EvidenceDocumentMapper {

    public EvidenceAssetDocument toDocument(EvidenceAsset asset) {
        if (asset == null) {
            return null;
        }

        return EvidenceAssetDocument.builder()
                .id(asset.getId().getValue())
                .incidentId(asset.getIncidentId())
                .type(asset.getType())
                .gridFsFileId(asset.getStorageReference().getGridFsFileId())
                .filename(asset.getStorageReference().getFilename())
                .integrityHashSha256(asset.getIntegrityHash().getSha256())
                .sizeBytes(asset.getSizeBytes())
                .contentType(asset.getContentType())
                .uploadedAt(asset.getUploadedAt())
                .retentionStatus(asset.getRetentionStatus())
                .purgeAt(asset.getPurgeAt())
                .build();
    }

    public EvidenceAsset toDomain(EvidenceAssetDocument doc) {
        if (doc == null) {
            return null;
        }

        return EvidenceAsset.reconstitute(
                EvidenceAssetId.of(doc.getId()),
                doc.getIncidentId(),
                doc.getType(),
                new StorageReference(doc.getGridFsFileId(), doc.getFilename()),
                new IntegrityHash(doc.getIntegrityHashSha256()),
                doc.getSizeBytes(),
                doc.getContentType(),
                doc.getUploadedAt(),
                doc.getRetentionStatus(),
                doc.getPurgeAt()
        );
    }
}
