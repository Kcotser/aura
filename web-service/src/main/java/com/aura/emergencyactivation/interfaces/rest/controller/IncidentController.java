package com.aura.emergencyactivation.interfaces.rest.controller;

import com.aura.emergencyactivation.application.usecase.ActivateIncidentUseCase;
import com.aura.emergencyactivation.application.usecase.CancelIncidentUseCase;
import com.aura.emergencyactivation.application.usecase.GetIncidentUseCase;
import com.aura.emergencyactivation.application.usecase.ListIncidentsUseCase;
import com.aura.emergencyactivation.application.usecase.TransitionIncidentUseCase;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.interfaces.rest.request.ActivateIncidentRequest;
import com.aura.emergencyactivation.interfaces.rest.request.CancelIncidentRequest;
import com.aura.emergencyactivation.interfaces.rest.request.TransitionIncidentRequest;
import com.aura.emergencyactivation.interfaces.rest.response.IncidentResponse;
import com.aura.iam.infrastructure.security.UserPrincipal;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for emergency incident activation and state machine lifecycle.
 */
@Tag(name = "Emergency Activation", description = "Safety incident activation and state machine management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final ActivateIncidentUseCase activateIncidentUseCase;
    private final CancelIncidentUseCase cancelIncidentUseCase;
    private final GetIncidentUseCase getIncidentUseCase;
    private final ListIncidentsUseCase listIncidentsUseCase;
    private final TransitionIncidentUseCase transitionIncidentUseCase;

    public IncidentController(
            ActivateIncidentUseCase activateIncidentUseCase,
            CancelIncidentUseCase cancelIncidentUseCase,
            GetIncidentUseCase getIncidentUseCase,
            ListIncidentsUseCase listIncidentsUseCase,
            TransitionIncidentUseCase transitionIncidentUseCase
    ) {
        this.activateIncidentUseCase = activateIncidentUseCase;
        this.cancelIncidentUseCase = cancelIncidentUseCase;
        this.getIncidentUseCase = getIncidentUseCase;
        this.listIncidentsUseCase = listIncidentsUseCase;
        this.transitionIncidentUseCase = transitionIncidentUseCase;
    }

    @Operation(summary = "Activate emergency incident", description = "Triggers an emergency incident in ACTIVATED status with GPS coordinates.")
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<IncidentResponse>> activate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ActivateIncidentRequest request
    ) {
        Incident incident = activateIncidentUseCase.execute(
                principal.getUserId(),
                request.latitude(),
                request.longitude()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(IncidentResponse.fromDomain(incident), "Emergency incident activated successfully"));
    }

    @Operation(summary = "Cancel emergency incident", description = "Cancels an active incident (false alarm).")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<IncidentResponse>> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody(required = false) CancelIncidentRequest request
    ) {
        String reason = (request != null) ? request.reason() : null;
        Incident incident = cancelIncidentUseCase.execute(id, principal.getUserId(), reason);
        return ResponseEntity.ok(ApiResponse.success(IncidentResponse.fromDomain(incident), "Incident cancelled successfully"));
    }

    @Operation(summary = "Get incident details", description = "Retrieves the detail and current state of a specific incident.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id
    ) {
        Incident incident = getIncidentUseCase.execute(id, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(IncidentResponse.fromDomain(incident)));
    }

    @Operation(summary = "List user incidents", description = "Retrieves a paginated list of emergency incidents for the authenticated user.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<Incident> incidents = listIncidentsUseCase.execute(principal.getUserId(), status, page, size);
        List<IncidentResponse> response = incidents.stream()
                .map(IncidentResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Manual status transition (Hackathon Demo Shortcut)",
            description = "Forces a manual status transition on an incident. Documented as a demo shortcut for testing state transitions.")
    @PostMapping("/{id}/transition")
    public ResponseEntity<ApiResponse<IncidentResponse>> transition(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody TransitionIncidentRequest request
    ) {
        Incident incident = transitionIncidentUseCase.execute(
                id,
                principal.getUserId(),
                request.targetStatus(),
                request.reason()
        );
        return ResponseEntity.ok(ApiResponse.success(IncidentResponse.fromDomain(incident), "Incident state transitioned successfully"));
    }
}
