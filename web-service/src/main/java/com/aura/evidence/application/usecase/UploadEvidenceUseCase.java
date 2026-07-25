package com.aura.evidence.application.usecase;

import com.aura.evidence.domain.event.AllEvidenceUploadedEvent;
import com.aura.evidence.domain.event.MediaReceivedEvent;
import com.aura.evidence.domain.event.MediaValidatedEvent;
import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.IntegrityHash;
import com.aura.evidence.domain.model.MediaType;
import com.aura.evidence.domain.model.StorageReference;
import com.aura.evidence.domain.repository.EvidenceRepository;
import com.aura.evidence.domain.repository.MediaStorageService;
import com.aura.shared.domain.exception.BusinessRuleViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Use case for uploading media evidence streams (frontCamera, backCamera, ambientAudio) for an incident.
 * Calculates SHA-256 integrity hash, stores files in GridFS, and checks if all 3 streams are received.
 */
@Service
public class UploadEvidenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadEvidenceUseCase.class);

    private final EvidenceRepository evidenceRepository;
    private final MediaStorageService mediaStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public UploadEvidenceUseCase(
            EvidenceRepository evidenceRepository,
            MediaStorageService mediaStorageService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.evidenceRepository = evidenceRepository;
        this.mediaStorageService = mediaStorageService;
        this.eventPublisher = eventPublisher;
    }

    public List<EvidenceAsset> execute(
            String incidentId,
            MultipartFile frontCamera,
            MultipartFile backCamera,
            MultipartFile ambientAudio
    ) {
        if (frontCamera == null && backCamera == null && ambientAudio == null) {
            throw new BusinessRuleViolationException("At least one evidence file must be provided");
        }

        List<EvidenceAsset> savedAssets = new ArrayList<>();

        if (frontCamera != null && !frontCamera.isEmpty()) {
            savedAssets.add(processFile(incidentId, MediaType.FRONT_CAMERA, frontCamera));
        }
        if (backCamera != null && !backCamera.isEmpty()) {
            savedAssets.add(processFile(incidentId, MediaType.BACK_CAMERA, backCamera));
        }
        if (ambientAudio != null && !ambientAudio.isEmpty()) {
            savedAssets.add(processFile(incidentId, MediaType.AMBIENT_AUDIO, ambientAudio));
        }

        // Check if all 3 streams exist for this incidentId
        checkAndPublishAllUploaded(incidentId);

        return savedAssets;
    }

    private EvidenceAsset processFile(String incidentId, MediaType type, MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream is = file.getInputStream();
            DigestInputStream dis = new DigestInputStream(is, digest);

            // Store file in GridFS while computing hash
            String filename = String.format("%s_%s_%s", incidentId, type.name().toLowerCase(), file.getOriginalFilename());
            StorageReference storageRef = mediaStorageService.store(filename, file.getContentType(), dis);

            String sha256Hex = HexFormat.of().formatHex(digest.digest());
            IntegrityHash integrityHash = new IntegrityHash(sha256Hex);

            EvidenceAsset asset = EvidenceAsset.create(
                    incidentId,
                    type,
                    storageRef,
                    integrityHash,
                    file.getSize(),
                    file.getContentType()
            );

            EvidenceAsset saved = evidenceRepository.save(asset);
            log.info("Evidence asset uploaded: id={}, incidentId={}, type={}, sha256={}",
                    saved.getId(), incidentId, type, sha256Hex);

            eventPublisher.publishEvent(new MediaReceivedEvent(saved.getId(), incidentId, type));
            eventPublisher.publishEvent(new MediaValidatedEvent(saved.getId(), incidentId, sha256Hex));

            return saved;
        } catch (Exception e) {
            log.error("Failed to process evidence upload for incidentId={}, type={}", incidentId, type, e);
            throw new BusinessRuleViolationException("Failed to upload evidence file: " + e.getMessage());
        }
    }

    private void checkAndPublishAllUploaded(String incidentId) {
        List<EvidenceAsset> assets = evidenceRepository.findByIncidentId(incidentId);
        boolean hasFront = assets.stream().anyMatch(a -> a.getType() == MediaType.FRONT_CAMERA);
        boolean hasBack = assets.stream().anyMatch(a -> a.getType() == MediaType.BACK_CAMERA);
        boolean hasAudio = assets.stream().anyMatch(a -> a.getType() == MediaType.AMBIENT_AUDIO);

        if (hasFront && hasBack && hasAudio) {
            log.info("All 3 evidence streams received for incidentId: {}. Publishing AllEvidenceUploadedEvent", incidentId);
            eventPublisher.publishEvent(new AllEvidenceUploadedEvent(incidentId));
        }
    }
}
