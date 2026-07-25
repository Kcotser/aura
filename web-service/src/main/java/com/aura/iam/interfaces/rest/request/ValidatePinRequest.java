package com.aura.iam.interfaces.rest.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for PIN validation (biometric fallback).
 */
public record ValidatePinRequest(
        @NotBlank(message = "PIN must not be blank")
        String pin
) {}
