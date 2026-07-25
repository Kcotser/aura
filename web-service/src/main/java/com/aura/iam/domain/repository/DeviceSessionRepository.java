package com.aura.iam.domain.repository;

import com.aura.iam.domain.model.DeviceSession;
import com.aura.iam.domain.model.DeviceSessionId;
import com.aura.iam.domain.model.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository port (interface) for DeviceSession persistence.
 *
 * <p>Implementations must support indexed lookup by refresh token hash
 * for the token refresh flow.
 */
public interface DeviceSessionRepository {

    /** Persists a new or updated DeviceSession. */
    DeviceSession save(DeviceSession session);

    /** Finds a DeviceSession by its ID. */
    Optional<DeviceSession> findById(DeviceSessionId id);

    /**
     * Finds a DeviceSession by the BCrypt hash of the refresh token.
     * Used in the token refresh flow.
     */
    Optional<DeviceSession> findByRefreshTokenHash(String refreshTokenHash);

    /** Returns all sessions for a given user. */
    List<DeviceSession> findAllByUserId(UserId userId);

    /** Persists multiple sessions at once (e.g., bulk revocation). */
    void saveAll(List<DeviceSession> sessions);
}
