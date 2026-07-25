package com.aura.iam.domain.event;

import com.aura.iam.domain.model.UserId;
import java.time.Instant;

/**
 * Domain event published when a new user successfully registers.
 *
 * <p>Other bounded contexts (e.g., trustnetwork for welcome notifications)
 * may listen to this event via {@code @ApplicationModuleListener}.
 */
public record UserRegisteredEvent(
        UserId userId,
        String email,
        String firstName,
        Instant occurredAt
) {
    public UserRegisteredEvent(UserId userId, String email, String firstName) {
        this(userId, email, firstName, Instant.now());
    }
}
