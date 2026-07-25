package com.aura.iam.interfaces.rest.request;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for partial profile updates.
 * All fields are optional (null = no change).
 */
public record UpdateProfileRequest(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        String phoneNumber
) {}
