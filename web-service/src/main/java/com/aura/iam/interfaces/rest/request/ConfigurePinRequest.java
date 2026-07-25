package com.aura.iam.interfaces.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for configuring or updating the backup PIN.
 */
public record ConfigurePinRequest(
        @NotBlank(message = "PIN must not be blank")
        @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4 to 6 digits")
        String pin
) {}
