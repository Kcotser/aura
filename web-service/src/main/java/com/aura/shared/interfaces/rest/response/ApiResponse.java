package com.aura.shared.interfaces.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Generic API response envelope.
 *
 * <p>All REST endpoints wrap their payload in this type to provide a consistent
 * response structure across the entire Aura API:
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": null,
 *   "timestamp": "2026-07-25T05:00:00Z"
 * }
 * </pre>
 *
 * @param <T> the type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp
) {

    /** Creates a successful response with a data payload. */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    /** Creates a successful response with a message and no data payload. */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message, Instant.now());
    }

    /** Creates a successful response with both data and a message. */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }

    /** Creates a failure response with an error message and no data. */
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, null, message, Instant.now());
    }
}
