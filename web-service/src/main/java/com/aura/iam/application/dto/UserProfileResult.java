package com.aura.iam.application.dto;

import java.time.Instant;

/**
 * Result DTO carrying user profile data to the interfaces layer.
 */
public record UserProfileResult(
        String id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean pinConfigured,
        boolean biometricEnabled,
        Instant createdAt
) {}
