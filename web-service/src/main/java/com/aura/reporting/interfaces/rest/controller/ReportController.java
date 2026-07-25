package com.aura.reporting.interfaces.rest.controller;

import com.aura.reporting.application.usecase.ApproveReportUseCase;
import com.aura.reporting.application.usecase.EditReportUseCase;
import com.aura.reporting.application.usecase.ExportReportPdfUseCase;
import com.aura.reporting.application.usecase.GetReportUseCase;
import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.interfaces.rest.request.EditReportRequest;
import com.aura.reporting.interfaces.rest.response.IncidentReportResponse;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Incident Reporting endpoints (`/api/v1/incidents/{id}/report`).
 */
@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/report")
@Tag(name = "Incident Reporting", description = "Endpoints for managing, editing, approving, and exporting incident legal reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final GetReportUseCase getReportUseCase;
    private final EditReportUseCase editReportUseCase;
    private final ApproveReportUseCase approveReportUseCase;
    private final ExportReportPdfUseCase exportReportPdfUseCase;

    public ReportController(
            GetReportUseCase getReportUseCase,
            EditReportUseCase editReportUseCase,
            ApproveReportUseCase approveReportUseCase,
            ExportReportPdfUseCase exportReportPdfUseCase) {
        this.getReportUseCase = getReportUseCase;
        this.editReportUseCase = editReportUseCase;
        this.approveReportUseCase = approveReportUseCase;
        this.exportReportPdfUseCase = exportReportPdfUseCase;
    }

    @GetMapping
    @Operation(summary = "Get current incident report draft")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> getReport(@PathVariable("incidentId") String incidentId) {
        IncidentReport report = getReportUseCase.execute(incidentId);
        return ResponseEntity.ok(ApiResponse.success(IncidentReportResponse.from(report)));
    }

    @PatchMapping
    @Operation(summary = "Edit fields of an incident report draft")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> editReport(
            @PathVariable("incidentId") String incidentId,
            @RequestBody EditReportRequest request) {
        IncidentReport report = editReportUseCase.execute(
                incidentId,
                request.audioTranscriptSummary(),
                request.visualContextDescription(),
                request.suggestedEntityCode()
        );
        return ResponseEntity.ok(ApiResponse.success(IncidentReportResponse.from(report)));
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve current incident report draft")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> approveReport(@PathVariable("incidentId") String incidentId) {
        IncidentReport report = approveReportUseCase.execute(incidentId);
        return ResponseEntity.ok(ApiResponse.success(IncidentReportResponse.from(report)));
    }

    @PostMapping("/export")
    @Operation(summary = "Generate and download incident report PDF")
    public ResponseEntity<byte[]> exportPdf(@PathVariable("incidentId") String incidentId) {
        byte[] pdfBytes = exportReportPdfUseCase.execute(incidentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + incidentId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
