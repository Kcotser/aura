package com.aura.evidence.interfaces.rest.controller;

import com.aura.emergencyactivation.application.usecase.GetIncidentUseCase;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.evidence.application.usecase.DownloadEvidenceUseCase;
import com.aura.evidence.application.usecase.ListEvidenceUseCase;
import com.aura.evidence.application.usecase.PurgeEvidenceUseCase;
import com.aura.evidence.application.usecase.UploadEvidenceUseCase;
import com.aura.evidence.domain.model.EvidenceAsset;
import com.aura.evidence.interfaces.rest.response.EvidenceAssetResponse;
import com.aura.iam.infrastructure.security.UserPrincipal;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for evidence upload, metadata retrieval, binary streaming, and retention management.
 */
@Tag(name = "Evidence Management", description = "Multipart upload, metadata inspection, streaming download, and retention purging")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping
public class EvidenceController {

    private final UploadEvidenceUseCase uploadEvidenceUseCase;
    private final ListEvidenceUseCase listEvidenceUseCase;
    private final DownloadEvidenceUseCase downloadEvidenceUseCase;
    private final PurgeEvidenceUseCase purgeEvidenceUseCase;
    private final GetIncidentUseCase getIncidentUseCase;

    public EvidenceController(
            UploadEvidenceUseCase uploadEvidenceUseCase,
            ListEvidenceUseCase listEvidenceUseCase,
            DownloadEvidenceUseCase downloadEvidenceUseCase,
            PurgeEvidenceUseCase purgeEvidenceUseCase,
            GetIncidentUseCase getIncidentUseCase
    ) {
        this.uploadEvidenceUseCase = uploadEvidenceUseCase;
        this.listEvidenceUseCase = listEvidenceUseCase;
        this.downloadEvidenceUseCase = downloadEvidenceUseCase;
        this.purgeEvidenceUseCase = purgeEvidenceUseCase;
        this.getIncidentUseCase = getIncidentUseCase;
    }

    @Operation(summary = "Upload evidence streams", description = "Multipart upload of up to 3 evidence files (frontCamera, backCamera, ambientAudio) for an incident.")
    @PostMapping(value = "/api/v1/incidents/{incidentId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<EvidenceAssetResponse>>> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String incidentId,
            @Parameter(description = "Front camera video file")
            @RequestPart(value = "frontCamera", required = false) MultipartFile frontCamera,
            @Parameter(description = "Back camera video file")
            @RequestPart(value = "backCamera", required = false) MultipartFile backCamera,
            @Parameter(description = "Ambient audio file")
            @RequestPart(value = "ambientAudio", required = false) MultipartFile ambientAudio
    ) {
        // Validate user ownership of incident
        getIncidentUseCase.execute(incidentId, principal.getUserId());

        List<EvidenceAsset> assets = uploadEvidenceUseCase.execute(
                incidentId,
                frontCamera,
                backCamera,
                ambientAudio
        );

        List<EvidenceAssetResponse> response = assets.stream()
                .map(EvidenceAssetResponse::fromDomain)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evidence uploaded successfully"));
    }

    @Operation(summary = "List incident evidence metadata", description = "Retrieves evidence metadata list for an incident (metadata only, no binary content).")
    @GetMapping("/api/v1/incidents/{incidentId}/evidence")
    public ResponseEntity<ApiResponse<List<EvidenceAssetResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String incidentId
    ) {
        getIncidentUseCase.execute(incidentId, principal.getUserId());

        List<EvidenceAsset> assets = listEvidenceUseCase.execute(incidentId);
        List<EvidenceAssetResponse> response = assets.stream()
                .map(EvidenceAssetResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Download original evidence file", description = "Streams the original binary media file from GridFS. Restricted to incident owner.")
    @GetMapping("/api/v1/evidence/{id}/download")
    public ResponseEntity<InputStreamResource> download(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id
    ) {
        DownloadEvidenceUseCase.DownloadResult result = downloadEvidenceUseCase.execute(id);

        // Security check: verify requesting user owns the incident associated with this evidence
        getIncidentUseCase.execute(result.asset().getIncidentId(), principal.getUserId());

        InputStreamResource resource = new InputStreamResource(result.inputStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.asset().getStorageReference().getFilename() + "\"")
                .contentType(MediaType.parseMediaType(result.asset().getContentType()))
                .contentLength(result.asset().getSizeBytes())
                .body(resource);
    }

    @Operation(summary = "Schedule evidence purge", description = "Schedules an evidence asset for retention purge.")
    @DeleteMapping("/api/v1/evidence/{id}")
    public ResponseEntity<ApiResponse<EvidenceAssetResponse>> purge(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id
    ) {
        DownloadEvidenceUseCase.DownloadResult downloadResult = downloadEvidenceUseCase.execute(id);
        getIncidentUseCase.execute(downloadResult.asset().getIncidentId(), principal.getUserId());

        EvidenceAsset purgedAsset = purgeEvidenceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(EvidenceAssetResponse.fromDomain(purgedAsset), "Evidence scheduled for purge"));
    }
}
