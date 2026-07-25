package com.aura.shared.interfaces.rest.response;

import java.time.Instant;
import java.util.List;

/**
 * Structured error response returned by the global exception handler.
 *
 * <p>Example:
 * <pre>
 * {
 *   "code": "VALIDATION_ERROR",
 *   "message": "Input validation failed",
 *   "details": ["email must not be blank", "password must be at least 8 characters"],
 *   "timestamp": "2026-07-25T05:00:00Z"
 * }
 * </pre>
 */
public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        Instant timestamp
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null, Instant.now());
    }

    public static ErrorResponse of(String code, String message, List<String> details) {
        return new ErrorResponse(code, message, details, Instant.now());
    }
}
