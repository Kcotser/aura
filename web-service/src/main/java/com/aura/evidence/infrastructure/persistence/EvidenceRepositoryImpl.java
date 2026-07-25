package com.aura.evidence.infrastructure.persistence;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.model.MediaType;
import com.aura.evidence.domain.repository.EvidenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EvidenceRepositoryImpl implements EvidenceRepository {

    private final SpringDataMongoEvidenceRepository springDataRepository;
    private final EvidenceDocumentMapper mapper;

    public EvidenceRepositoryImpl(SpringDataMongoEvidenceRepository springDataRepository, EvidenceDocumentMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public EvidenceAsset save(EvidenceAsset asset) {
        EvidenceAssetDocument doc = mapper.toDocument(asset);
        EvidenceAssetDocument saved = springDataRepository.save(doc);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EvidenceAsset> findById(EvidenceAssetId id) {
        return springDataRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<EvidenceAsset> findByIncidentId(String incidentId) {
        return springDataRepository.findByIncidentId(incidentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<EvidenceAsset> findByIncidentIdAndType(String incidentId, MediaType type) {
        return springDataRepository.findByIncidentIdAndType(incidentId, type).map(mapper::toDomain);
    }

    @Override
    public void delete(EvidenceAsset asset) {
        springDataRepository.deleteById(asset.getId().getValue());
    }
}
