package com.aura.evidence.application.usecase;

import com.aura.evidence.domain.event.MediaPurgedEvent;
import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.repository.EvidenceRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Use case to schedule evidence asset purge.
 */
@Service
public class PurgeEvidenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(PurgeEvidenceUseCase.class);

    private final EvidenceRepository evidenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PurgeEvidenceUseCase(EvidenceRepository evidenceRepository, ApplicationEventPublisher eventPublisher) {
        this.evidenceRepository = evidenceRepository;
        this.eventPublisher = eventPublisher;
    }

    public EvidenceAsset execute(String assetIdStr) {
        EvidenceAssetId assetId = EvidenceAssetId.of(assetIdStr);
        EvidenceAsset asset = evidenceRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("EvidenceAsset", assetIdStr));

        asset.schedulePurge();
        EvidenceAsset saved = evidenceRepository.save(asset);
        log.info("Evidence asset marked PURGE_SCHEDULED: id={}, incidentId={}", saved.getId(), saved.getIncidentId());

        eventPublisher.publishEvent(new MediaPurgedEvent(saved.getId(), saved.getIncidentId()));
        return saved;
    }
}
