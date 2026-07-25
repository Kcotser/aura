package com.aura.iam.application.dto;

/**
 * Command DTO for refreshing an access token.
 */
public record RefreshTokenCommand(
        String refreshToken,
        String deviceId
) {}
