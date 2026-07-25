package com.aura.iam.domain.event;

import com.aura.iam.domain.model.UserId;
import java.time.Instant;

/**
 * Domain event published when a user requests account deletion.
 *
 * <p>The {@code privacy} context listens to this event to trigger
 * data retention policies and schedule evidence purging.
 */
public record AccountDeletionRequestedEvent(
        UserId userId,
        String email,
        Instant occurredAt
) {
    public AccountDeletionRequestedEvent(UserId userId, String email) {
        this(userId, email, Instant.now());
    }
}
