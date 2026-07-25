package com.aura.iam.application.dto;

/**
 * Result DTO containing both access and refresh tokens after successful authentication.
 */
public record AuthTokenResult(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        String tokenType
) {
    public AuthTokenResult(String accessToken, String refreshToken, long accessTokenExpiresInSeconds) {
        this(accessToken, refreshToken, accessTokenExpiresInSeconds, "Bearer");
    }
}
