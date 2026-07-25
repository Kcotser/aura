package com.aura.evidence.application.service;

import com.aura.evidence.application.dto.EvidenceDownloadReference;
import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.repository.EvidenceRepository;
import com.aura.evidence.domain.repository.MediaStorageService;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Public application query service for the Evidence bounded context.
 * Exposes evidence download references and binary stream access for analysis processing.
 */
@Service
public class EvidenceQueryService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceQueryService.class);

    private final EvidenceRepository evidenceRepository;
    private final MediaStorageService mediaStorageService;

    public EvidenceQueryService(EvidenceRepository evidenceRepository, MediaStorageService mediaStorageService) {
        this.evidenceRepository = evidenceRepository;
        this.mediaStorageService = mediaStorageService;
    }

    public List<EvidenceDownloadReference> getDownloadReferencesForIncident(String incidentId) {
        return evidenceRepository.findByIncidentId(incidentId).stream()
                .map(asset -> new EvidenceDownloadReference(
                        asset.getId().getValue(),
                        asset.getIncidentId(),
                        asset.getType().name(),
                        asset.getStorageReference() != null ? asset.getStorageReference().getGridFsFileId() : null,
                        asset.getStorageReference() != null ? asset.getStorageReference().getFilename() : null,
                        asset.getIntegrityHash() != null ? asset.getIntegrityHash().getSha256() : null,
                        asset.getContentType(),
                        asset.getSizeBytes()
                ))
                .toList();
    }

    /**
     * Reads the raw binary bytes of an evidence asset from GridFS for multimodal AI analysis.
     *
     * @param evidenceId the EvidenceAsset ID
     * @return raw binary content byte array
     */
    public byte[] readEvidenceBytes(String evidenceId) {
        EvidenceAsset asset = evidenceRepository.findById(EvidenceAssetId.of(evidenceId))
                .orElseThrow(() -> new ResourceNotFoundException("EvidenceAsset", evidenceId));

        if (asset.getStorageReference() == null) {
            return new byte[0];
        }

        try (InputStream is = mediaStorageService.load(asset.getStorageReference())) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read evidence binary for id=" + evidenceId, e);
        }
    }

    /**
     * Loads evidence content safely as an Optional.
     */
    public Optional<byte[]> loadContent(String evidenceId) {
        Optional<EvidenceAsset> asset = evidenceRepository.findById(EvidenceAssetId.of(evidenceId));
        if (asset.isEmpty() || asset.get().getStorageReference() == null) {
            log.warn("No storage reference found for evidenceId={}", evidenceId);
            return Optional.empty();
        }

        try (InputStream stream = mediaStorageService.load(asset.get().getStorageReference())) {
            return Optional.of(stream.readAllBytes());
        } catch (Exception e) {
            log.warn("Failed to load evidence content for evidenceId={}: {}", evidenceId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the MIME content type of an evidence asset.
     */
    public String getContentType(String evidenceId) {
        return evidenceRepository.findById(EvidenceAssetId.of(evidenceId))
                .map(EvidenceAsset::getContentType)
                .orElse("application/octet-stream");
    }
}


