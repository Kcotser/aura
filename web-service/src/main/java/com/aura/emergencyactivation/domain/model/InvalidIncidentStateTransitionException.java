package com.aura.emergencyactivation.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

/**
 * Exception thrown when attempting an illegal state transition on an {@link Incident}.
 */
public class InvalidIncidentStateTransitionException extends BusinessRuleViolationException {

    public InvalidIncidentStateTransitionException(IncidentStatus currentStatus, IncidentStatus targetStatus) {
        super(String.format("Invalid state transition for incident: cannot transition from %s to %s",
                currentStatus, targetStatus));
    }

    public InvalidIncidentStateTransitionException(String message) {
        super(message);
    }
}
