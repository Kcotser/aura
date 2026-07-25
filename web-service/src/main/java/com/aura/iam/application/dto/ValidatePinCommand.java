package com.aura.iam.application.dto;

/**
 * Command DTO for validating a backup PIN (biometric fallback).
 */
public record ValidatePinCommand(
        String userId,
        String pin
) {}
