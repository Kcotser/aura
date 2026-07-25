package com.aura.iam.interfaces.rest.controller;

import com.aura.iam.application.dto.*;
import com.aura.iam.application.usecase.*;
import com.aura.iam.infrastructure.security.UserPrincipal;
import com.aura.iam.interfaces.rest.request.*;
import com.aura.iam.interfaces.rest.response.AuthTokenResponse;
import com.aura.iam.interfaces.rest.response.UserProfileResponse;
import com.aura.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication-related endpoints.
 *
 * <p>Handles registration, login, token refresh, logout, and PIN management.
 * All endpoints live under {@code /api/v1/auth}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ConfigurePinUseCase configurePinUseCase;
    private final ValidatePinUseCase validatePinUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            ConfigurePinUseCase configurePinUseCase,
            ValidatePinUseCase validatePinUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.configurePinUseCase = configurePinUseCase;
        this.validatePinUseCase = validatePinUseCase;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserProfileResult result = registerUserUseCase.execute(new RegisterUserCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toProfileResponse(result), "Registration successful"));
    }

    @Operation(summary = "Authenticate user and obtain tokens")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokenResult result = authenticateUserUseCase.execute(new AuthenticateUserCommand(
                request.email(),
                request.password(),
                request.deviceId()
        ));
        return ResponseEntity.ok(ApiResponse.success(toTokenResponse(result)));
    }

    @Operation(summary = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthTokenResult result = refreshTokenUseCase.execute(new RefreshTokenCommand(
                request.refreshToken(),
                request.deviceId()
        ));
        return ResponseEntity.ok(ApiResponse.success(toTokenResponse(result)));
    }

    @Operation(summary = "Logout and revoke current session",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = request != null ? request.refreshToken() : null;
        logoutUseCase.execute(principal.getUserId(), refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @Operation(summary = "Configure or update backup PIN",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/pin")
    public ResponseEntity<ApiResponse<Void>> configurePin(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ConfigurePinRequest request
    ) {
        configurePinUseCase.execute(new ConfigurePinCommand(principal.getUserId(), request.pin()));
        return ResponseEntity.ok(ApiResponse.success("PIN configured successfully"));
    }

    @Operation(summary = "Validate backup PIN",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/pin/validate")
    public ResponseEntity<ApiResponse<Void>> validatePin(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ValidatePinRequest request
    ) {
        validatePinUseCase.execute(new ValidatePinCommand(principal.getUserId(), request.pin()));
        return ResponseEntity.ok(ApiResponse.success("PIN is valid"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private AuthTokenResponse toTokenResponse(AuthTokenResult result) {
        return new AuthTokenResponse(
                result.accessToken(),
                result.refreshToken(),
                result.accessTokenExpiresInSeconds(),
                result.tokenType()
        );
    }

    private UserProfileResponse toProfileResponse(UserProfileResult result) {
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
