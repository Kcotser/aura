package com.aura.iam.interfaces.rest.controller;

import com.aura.iam.application.dto.UpdateProfileCommand;
import com.aura.iam.application.dto.UserProfileResult;
import com.aura.iam.application.usecase.GetUserProfileUseCase;
import com.aura.iam.application.usecase.RequestAccountDeletionUseCase;
import com.aura.iam.application.usecase.UpdateUserProfileUseCase;
import com.aura.iam.infrastructure.security.UserPrincipal;
import com.aura.iam.interfaces.rest.request.UpdateProfileRequest;
import com.aura.iam.interfaces.rest.response.UserProfileResponse;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 *
 * <p>All endpoints require JWT authentication and operate on the
 * currently authenticated user's own profile.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile management endpoints")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final RequestAccountDeletionUseCase requestAccountDeletionUseCase;

    public UserController(
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            RequestAccountDeletionUseCase requestAccountDeletionUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.requestAccountDeletionUseCase = requestAccountDeletionUseCase;
    }

    @Operation(summary = "Get current user profile",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserProfileResult result = getUserProfileUseCase.execute(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(toResponse(result)));
    }

    @Operation(summary = "Update current user profile",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResult result = updateUserProfileUseCase.execute(new UpdateProfileCommand(
                principal.getUserId(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        ));
        return ResponseEntity.ok(ApiResponse.success(toResponse(result)));
    }

    @Operation(summary = "Request account deletion",
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        requestAccountDeletionUseCase.execute(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Account deletion has been requested"));
    }

    private UserProfileResponse toResponse(UserProfileResult result) {
        return new UserProfileResponse(
                result.id(),
                result.email(),
                result.firstName(),
                result.lastName(),
                result.phoneNumber(),
                result.pinConfigured(),
                result.biometricEnabled(),
                result.createdAt()
        );
    }
}
