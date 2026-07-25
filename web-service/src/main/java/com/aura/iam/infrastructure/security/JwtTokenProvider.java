package com.aura.iam.infrastructure.security;

import com.aura.iam.application.port.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Provides JWT token generation and validation for access and refresh tokens.
 *
 * <p>Access tokens are short-lived (default 15 minutes) and carry the userId
 * and email as claims. Refresh tokens are long-lived (default 30 days) and
 * only carry the userId as the subject.
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;

    public JwtTokenProvider(
            @Value("${aura.jwt.secret}") String secret,
            @Value("${aura.jwt.access-token-expiration-minutes:15}") long accessTokenExpirationMinutes,
            @Value("${aura.jwt.refresh-token-expiration-days:30}") long refreshTokenExpirationDays
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    /**
     * Generates a short-lived access token.
     *
     * @param userId the subject (user ID)
     * @param email  the user's email (stored as a claim)
     */
    public String generateAccessToken(String userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a long-lived refresh token (userId as subject only).
     *
     * @param userId the subject (user ID)
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationDays * 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates an access token and returns true if valid.
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the subject (userId) from a valid access token.
     */
    public String extractSubjectFromAccessToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the subject (userId) from a valid refresh token.
     */
    public String extractSubjectFromRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new JwtException("Token is not a refresh token");
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new com.aura.shared.domain.exception.AuthenticationFailedException(
                    "Invalid refresh token: " + e.getMessage());
        }
    }

    /**
     * Extracts the email claim from a valid access token.
     */
    public String extractEmailFromAccessToken(String token) {
        return parseClaims(token).get(CLAIM_EMAIL, String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpirationDays;
    }
}
