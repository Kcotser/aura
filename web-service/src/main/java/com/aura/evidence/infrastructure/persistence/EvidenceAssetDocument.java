package com.aura.evidence.infrastructure.persistence;

import com.aura.evidence.domain.model.MediaType;
import com.aura.evidence.domain.model.RetentionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for the evidence_assets collection.
 */
@Document(collection = "evidence_assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceAssetDocument {

    @Id
    private String id;

    @Indexed
    private String incidentId;

    private MediaType type;

    private String gridFsFileId;
    private String filename;

    private String integrityHashSha256;
    private long sizeBytes;
    private String contentType;
    private Instant uploadedAt;

    private RetentionStatus retentionStatus;

    @Indexed
    private Instant purgeAt;
}
