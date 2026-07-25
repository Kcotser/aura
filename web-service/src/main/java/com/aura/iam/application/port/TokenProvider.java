package com.aura.iam.application.port;

/**
 * Port (interface) for JWT token generation and validation.
 *
 * <p>Defined in the application layer so use cases remain infrastructure-agnostic.
 * The actual JWT implementation lives in {@code com.aura.iam.infrastructure.security.JwtTokenProvider},
 * which implements this interface.
 */
public interface TokenProvider {

    /**
     * Generates a short-lived access token.
     *
     * @param userId the subject (user ID)
     * @param email  the user's email address
     * @return signed JWT access token string
     */
    String generateAccessToken(String userId, String email);

    /**
     * Generates a long-lived refresh token.
     *
     * @param userId the subject (user ID)
     * @return signed JWT refresh token string
     */
    String generateRefreshToken(String userId);

    /**
     * Validates an access token.
     *
     * @param token the access token string
     * @return true if the token is valid and unexpired
     */
    boolean validateAccessToken(String token);

    /**
     * Extracts the subject (userId) from a valid access token.
     *
     * @param token the access token string
     * @return the userId from the token's subject claim
     */
    String extractSubjectFromAccessToken(String token);

    /**
     * Extracts the email claim from a valid access token.
     *
     * @param token the access token string
     * @return the email string from the token claims
     */
    String extractEmailFromAccessToken(String token);

    /**
     * Extracts the subject (userId) from a valid refresh token.
     *
     * @param token the refresh token string
     * @return the userId from the token's subject claim
     * @throws com.aura.shared.domain.exception.AuthenticationFailedException if the token is invalid
     */
    String extractSubjectFromRefreshToken(String token);

    /** Returns the configured access token expiration in minutes. */
    long getAccessTokenExpirationMinutes();

    /** Returns the configured refresh token expiration in days. */
    long getRefreshTokenExpirationDays();
}
