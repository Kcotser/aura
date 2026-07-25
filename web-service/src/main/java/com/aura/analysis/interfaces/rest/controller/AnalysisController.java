package com.aura.analysis.interfaces.rest.controller;

import com.aura.analysis.application.usecase.GetAnalysisJobUseCase;
import com.aura.analysis.application.usecase.RetryAnalysisUseCase;
import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.interfaces.rest.response.AnalysisJobResponse;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Multimodal AI Analysis endpoints (`/api/v1/incidents/{id}/analysis`).
 */
@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/analysis")
@Tag(name = "Incident Analysis",
     description = "Analisis multimodal de la evidencia: Gemini transcribe el audio y describe el "
             + "video, y Gemma 4 clasifica entidad de derivacion y nivel de amenaza")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {

    private final GetAnalysisJobUseCase getAnalysisJobUseCase;
    private final RetryAnalysisUseCase retryAnalysisUseCase;

    public AnalysisController(GetAnalysisJobUseCase getAnalysisJobUseCase, RetryAnalysisUseCase retryAnalysisUseCase) {
        this.getAnalysisJobUseCase = getAnalysisJobUseCase;
        this.retryAnalysisUseCase = retryAnalysisUseCase;
    }

    @GetMapping
    @Operation(summary = "Get AI analysis job status and result for an incident")
    public ResponseEntity<ApiResponse<AnalysisJobResponse>> getAnalysis(@PathVariable("incidentId") String incidentId) {
        AnalysisJob job = getAnalysisJobUseCase.execute(incidentId);
        return ResponseEntity.ok(ApiResponse.success(AnalysisJobResponse.from(job)));
    }

    @PostMapping("/retry")
    @Operation(summary = "Retry failed AI analysis job for an incident")
    public ResponseEntity<ApiResponse<AnalysisJobResponse>> retryAnalysis(@PathVariable("incidentId") String incidentId) {
        AnalysisJob job = retryAnalysisUseCase.execute(incidentId);
        return ResponseEntity.ok(ApiResponse.success(AnalysisJobResponse.from(job)));
    }
}
