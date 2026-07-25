package com.aura.emergencyactivation.domain.model;

/**
 * Aggregate Root for the Emergency Activation context.
 *
 * <p>Represents a safety incident with a state machine lifecycle:
 * {@code Activated → Recording → Uploaded → Processing → DraftReady → Approved → Closed}.
 *
 * TODO: implement after hackathon
 */
public class Incident {
    // TODO: implement after hackathon
    // Fields: id, userId, state (sealed interface IncidentState), activatedAt, closedAt, evidenceIds
    // Methods: activate(), startRecording(), markUploaded(), approve(), close()
}
