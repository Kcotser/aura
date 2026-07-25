package com.aura.evidence.application.usecase;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.repository.EvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case to list evidence metadata for an incident.
 */
@Service
public class ListEvidenceUseCase {

    private final EvidenceRepository evidenceRepository;

    public ListEvidenceUseCase(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    public List<EvidenceAsset> execute(String incidentId) {
        return evidenceRepository.findByIncidentId(incidentId);
    }
}
