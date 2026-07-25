package com.aura.iam.domain.model;

import java.time.Instant;

/**
 * Entity representing an active device session tied to a refresh token.
 *
 * <p>The raw refresh token is NEVER stored — only its BCrypt hash.
 * Belongs to the User aggregate but managed as a separate collection
 * for efficient lookup by token hash.
 */
public class DeviceSession {

    private final DeviceSessionId id;
    private final UserId userId;
    private String refreshTokenHash;
    private final String deviceId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private boolean revoked;

    private DeviceSession(
            DeviceSessionId id,
            UserId userId,
            String refreshTokenHash,
            String deviceId,
            Instant issuedAt,
            Instant expiresAt,
            boolean revoked
    ) {
        this.id = id;
        this.userId = userId;
        this.refreshTokenHash = refreshTokenHash;
        this.deviceId = deviceId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    /**
     * Factory method to create a new device session.
     *
     * @param userId           the owning user's ID
     * @param refreshTokenHash BCrypt hash of the raw refresh token
     * @param deviceId         identifier of the user's device
     * @param expiresAt        expiry timestamp for the refresh token
     */
    public static DeviceSession create(
            UserId userId,
            String refreshTokenHash,
            String deviceId,
            Instant expiresAt
    ) {
        return new DeviceSession(
                DeviceSessionId.generate(),
                userId,
                refreshTokenHash,
                deviceId,
                Instant.now(),
                expiresAt,
                false
        );
    }

    /** Reconstitutes a DeviceSession from persistence. */
    public static DeviceSession reconstitute(
            DeviceSessionId id,
            UserId userId,
            String refreshTokenHash,
            String deviceId,
            Instant issuedAt,
            Instant expiresAt,
            boolean revoked
    ) {
        return new DeviceSession(id, userId, refreshTokenHash, deviceId, issuedAt, expiresAt, revoked);
    }

    /** Revokes this session, invalidating the refresh token. */
    public void revoke() {
        this.revoked = true;
    }

    /** Returns true if this session is still valid (not revoked and not expired). */
    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }

    // Getters
    public DeviceSessionId getId() { return id; }
    public UserId getUserId() { return userId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public String getDeviceId() { return deviceId; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
}
