package com.aura.directory.interfaces.rest.controller;

import com.aura.directory.application.dto.InstitutionResult;
import com.aura.directory.application.usecase.GetInstitutionByCodeUseCase;
import com.aura.directory.application.usecase.ListInstitutionsUseCase;
import com.aura.directory.domain.model.InstitutionType;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the Institutional Directory.
 * Provides endpoints to list all institutions and get a single institution details by code.
 */
@Tag(name = "Institutional Directory", description = "Read-only access to the catalog of support institutions")
@RestController
@RequestMapping("/api/v1/institutions")
public class InstitutionController {

    private final ListInstitutionsUseCase listInstitutionsUseCase;
    private final GetInstitutionByCodeUseCase getInstitutionByCodeUseCase;

    public InstitutionController(
            ListInstitutionsUseCase listInstitutionsUseCase,
            GetInstitutionByCodeUseCase getInstitutionByCodeUseCase
    ) {
        this.listInstitutionsUseCase = listInstitutionsUseCase;
        this.getInstitutionByCodeUseCase = getInstitutionByCodeUseCase;
    }

    @Operation(summary = "List all institutions", description = "Retrieve list of all institutions, optionally filtered by type.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InstitutionResult>>> list(
            @Parameter(description = "Filter by type (HELPLINE, POLICE, SHELTER, NGO, OTHER)")
            @RequestParam(required = false) InstitutionType type
    ) {
        List<InstitutionResult> results = listInstitutionsUseCase.execute(type).stream()
                .map(InstitutionResult::fromDomain)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(
            summary = "Get institution by code",
            description = "Retrieve details for a specific institution by its unique string code. " +
                          "If the code is not found, a generic fallback (Línea 100) is returned automatically."
    )
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<InstitutionResult>> getByCode(
            @Parameter(description = "Unique identifier code (e.g. 'linea-100', 'pnp')")
            @PathVariable String code
    ) {
        InstitutionResult result = InstitutionResult.fromDomain(getInstitutionByCodeUseCase.execute(code));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
