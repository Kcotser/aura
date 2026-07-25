package com.aura.iam.domain.event;

import com.aura.iam.domain.model.UserId;
import java.time.Instant;

/**
 * Domain event published when a user configures or updates their backup PIN.
 */
public record PinConfiguredEvent(
        UserId userId,
        Instant occurredAt
) {
    public PinConfiguredEvent(UserId userId) {
        this(userId, Instant.now());
    }
}
