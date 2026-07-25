package com.aura.iam.application.dto;

/**
 * Command DTO for updating user profile fields.
 * Null fields are ignored (partial update semantics).
 */
public record UpdateProfileCommand(
        String userId,
        String firstName,
        String lastName,
        String phoneNumber
) {}
