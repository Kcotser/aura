package com.aura.iam.interfaces.rest.response;

/**
 * Response DTO containing JWT tokens returned after login or token refresh.
 */
public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String tokenType
) {}
