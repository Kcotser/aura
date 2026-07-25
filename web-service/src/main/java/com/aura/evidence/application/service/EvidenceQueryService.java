package com.aura.evidence.application.service;

import com.aura.evidence.application.dto.EvidenceDownloadReference;
import com.aura.evidence.domain.repository.EvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Public application query service for the Evidence bounded context.
 * Exposes evidence download references for analysis processing without leaking domain aggregates.
 */
@Service
public class EvidenceQueryService {

    private final EvidenceRepository evidenceRepository;

    public EvidenceQueryService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    public List<EvidenceDownloadReference> getDownloadReferencesForIncident(String incidentId) {
        return evidenceRepository.findByIncidentId(incidentId).stream()
                .map(asset -> new EvidenceDownloadReference(
                        asset.getId().getValue(),
                        asset.getIncidentId(),
                        asset.getType().name(),
                        asset.getStorageReference() != null ? asset.getStorageReference().getGridFsFileId() : null,
                        asset.getStorageReference() != null ? asset.getStorageReference().getFilename() : null,
                        asset.getIntegrityHash() != null ? asset.getIntegrityHash().getSha256() : null
                ))
                .toList();
    }
}
