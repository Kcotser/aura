package com.aura.evidence.domain.repository;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.model.MediaType;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link EvidenceAsset} aggregate operations.
 */
public interface EvidenceRepository {

    EvidenceAsset save(EvidenceAsset asset);

    Optional<EvidenceAsset> findById(EvidenceAssetId id);

    List<EvidenceAsset> findByIncidentId(String incidentId);

    Optional<EvidenceAsset> findByIncidentIdAndType(String incidentId, MediaType type);

    void delete(EvidenceAsset asset);
}
