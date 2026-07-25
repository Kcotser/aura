package com.aura.iam.application.dto;

/**
 * Command DTO for configuring or updating a backup PIN.
 */
public record ConfigurePinCommand(
        String userId,
        String pin
) {}
