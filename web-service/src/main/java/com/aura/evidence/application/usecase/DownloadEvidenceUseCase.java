package com.aura.evidence.application.usecase;

import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.domain.model.EvidenceAssetId;
import com.aura.evidence.domain.repository.EvidenceRepository;
import com.aura.evidence.domain.repository.MediaStorageService;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Use case to download binary content of an evidence file from GridFS.
 */
@Service
public class DownloadEvidenceUseCase {

    private final EvidenceRepository evidenceRepository;
    private final MediaStorageService mediaStorageService;

    public DownloadEvidenceUseCase(EvidenceRepository evidenceRepository, MediaStorageService mediaStorageService) {
        this.evidenceRepository = evidenceRepository;
        this.mediaStorageService = mediaStorageService;
    }

    public DownloadResult execute(String assetIdStr) {
        EvidenceAssetId assetId = EvidenceAssetId.of(assetIdStr);
        EvidenceAsset asset = evidenceRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("EvidenceAsset", assetIdStr));

        InputStream stream = mediaStorageService.load(asset.getStorageReference());
        return new DownloadResult(asset, stream);
    }

    public record DownloadResult(EvidenceAsset asset, InputStream inputStream) {}
}
