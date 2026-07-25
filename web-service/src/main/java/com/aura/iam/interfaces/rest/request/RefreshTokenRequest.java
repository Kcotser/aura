package com.aura.iam.interfaces.rest.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for access token renewal.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token must not be blank")
        String refreshToken,

        String deviceId
) {}
