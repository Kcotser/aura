package com.aura.iam.interfaces.rest.response;

import java.time.Instant;

/**
 * Response DTO for user profile data.
 */
public record UserProfileResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean pinConfigured,
        boolean biometricEnabled,
        Instant createdAt
) {}
