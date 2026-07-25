package com.aura.iam.domain.event;

import com.aura.iam.domain.model.UserId;
import java.time.Instant;

/**
 * Domain event published when a user successfully authenticates.
 *
 * <p>Can be consumed by the privacy context for access audit logging.
 */
public record UserAuthenticatedEvent(
        UserId userId,
        String deviceId,
        Instant occurredAt
) {
    public UserAuthenticatedEvent(UserId userId, String deviceId) {
        this(userId, deviceId, Instant.now());
    }
}
