package com.aura.emergencyactivation.domain.model;

/**
 * State machine status enum for safety incidents.
 *
 * <p>Lifecycle flow:
 * ACTIVATED → RECORDING → UPLOADED → PROCESSING → DRAFT_READY → APPROVED → EXPORTED → CLOSED
 * plus lateral CANCELLED (false alarm).
 */
public enum IncidentStatus {
    ACTIVATED,
    RECORDING,
    UPLOADED,
    PROCESSING,
    DRAFT_READY,
    APPROVED,
    EXPORTED,
    CLOSED,
    CANCELLED;

    /**
     * Checks whether a transition to {@code targetStatus} is legally allowed from the current status.
     */
    public boolean canTransitionTo(IncidentStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        // Terminal states cannot transition further
        if (this == CLOSED || this == CANCELLED) {
            return false;
        }

        // False alarm cancellation is allowed from any active state prior to CLOSED
        if (targetStatus == CANCELLED) {
            return true;
        }

        return switch (this) {
            case ACTIVATED -> targetStatus == RECORDING || targetStatus == UPLOADED || targetStatus == PROCESSING || targetStatus == DRAFT_READY;
            case RECORDING -> targetStatus == UPLOADED || targetStatus == PROCESSING || targetStatus == DRAFT_READY;
            case UPLOADED -> targetStatus == PROCESSING || targetStatus == DRAFT_READY;
            case PROCESSING -> targetStatus == DRAFT_READY || targetStatus == APPROVED;
            case DRAFT_READY -> targetStatus == APPROVED;
            case APPROVED -> targetStatus == EXPORTED || targetStatus == CLOSED;
            case EXPORTED -> targetStatus == CLOSED;
            case CLOSED, CANCELLED -> false;
        };
    }
}
