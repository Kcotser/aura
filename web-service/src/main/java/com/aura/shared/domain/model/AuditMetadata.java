package com.aura.shared.domain.model;

import java.time.Instant;

/**
 * Reusable audit metadata value object.
 *
 * <p>Embedded in domain aggregates to track creation and last-modification
 * timestamps without coupling the domain to any persistence framework.
 */
public record AuditMetadata(Instant createdAt, Instant updatedAt) {

    /** Factory: creates metadata with both timestamps set to now. */
    public static AuditMetadata now() {
        Instant ts = Instant.now();
        return new AuditMetadata(ts, ts);
    }

    /** Returns a new instance with updatedAt refreshed to now. */
    public AuditMetadata updated() {
        return new AuditMetadata(createdAt, Instant.now());
    }
}
