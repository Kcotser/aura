package com.aura.evidence.infrastructure.persistence;

import com.aura.evidence.domain.model.MediaType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataMongoEvidenceRepository extends MongoRepository<EvidenceAssetDocument, String> {

    List<EvidenceAssetDocument> findByIncidentId(String incidentId);

    Optional<EvidenceAssetDocument> findByIncidentIdAndType(String incidentId, MediaType type);
}
