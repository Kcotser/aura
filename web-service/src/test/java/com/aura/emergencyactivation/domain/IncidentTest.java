package com.aura.emergencyactivation.domain;

import com.aura.emergencyactivation.domain.model.GpsLocation;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.model.InvalidIncidentStateTransitionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Incident Aggregate State Machine Unit Tests")
class IncidentTest {

    private final String userId = "user-12345";
    private final GpsLocation location = new GpsLocation(-12.046374, -77.042793);

    @Test
    @DisplayName("Should activate incident in ACTIVATED status with initial status history")
    void shouldActivateIncident() {
        Incident incident = Incident.activate(userId, location);

        assertNotNull(incident.getId());
        assertEquals(userId, incident.getUserId());
        assertEquals(IncidentStatus.ACTIVATED, incident.getStatus());
        assertEquals(location, incident.getGpsLocation());
        assertNotNull(incident.getActivatedAt());
        assertNull(incident.getClosedAt());
        assertEquals(1, incident.getStatusHistory().size());
        assertEquals(IncidentStatus.ACTIVATED, incident.getStatusHistory().get(0).toStatus());
    }

    @Test
    @DisplayName("Should transition cleanly through legal lifecycle: ACTIVATED -> RECORDING -> UPLOADED -> PROCESSING -> DRAFT_READY -> APPROVED -> EXPORTED -> CLOSED")
    void shouldFollowLegalLifecycle() {
        Incident incident = Incident.activate(userId, location);

        incident.markRecording();
        assertEquals(IncidentStatus.RECORDING, incident.getStatus());

        incident.markUploaded();
        assertEquals(IncidentStatus.UPLOADED, incident.getStatus());

        incident.markProcessing();
        assertEquals(IncidentStatus.PROCESSING, incident.getStatus());

        incident.markDraftReady();
        assertEquals(IncidentStatus.DRAFT_READY, incident.getStatus());

        incident.approve();
        assertEquals(IncidentStatus.APPROVED, incident.getStatus());

        incident.export();
        assertEquals(IncidentStatus.EXPORTED, incident.getStatus());

        incident.close();
        assertEquals(IncidentStatus.CLOSED, incident.getStatus());
        assertNotNull(incident.getClosedAt());
        assertEquals(8, incident.getStatusHistory().size());
    }

    @Test
    @DisplayName("Should allow direct transition from ACTIVATED to UPLOADED")
    void shouldAllowDirectTransitionToUploaded() {
        Incident incident = Incident.activate(userId, location);

        incident.markUploaded();

        assertEquals(IncidentStatus.UPLOADED, incident.getStatus());
    }

    @Test
    @DisplayName("Should fail when attempting illegal state transition from ACTIVATED directly to APPROVED")
    void shouldFailIllegalTransitionToApproved() {
        Incident incident = Incident.activate(userId, location);

        assertThrows(InvalidIncidentStateTransitionException.class, incident::approve);
        assertEquals(IncidentStatus.ACTIVATED, incident.getStatus());
    }

    @Test
    @DisplayName("Should fail when attempting to transition a CLOSED incident")
    void shouldFailTransitioningClosedIncident() {
        Incident incident = Incident.activate(userId, location);
        incident.markUploaded();
        incident.markProcessing();
        incident.markDraftReady();
        incident.approve();
        incident.close();

        assertThrows(InvalidIncidentStateTransitionException.class, incident::markRecording);
    }

    @Test
    @DisplayName("Should allow cancellation from active status (false alarm)")
    void shouldAllowCancellationFromActiveState() {
        Incident incident = Incident.activate(userId, location);

        incident.cancel("Accidental press");

        assertEquals(IncidentStatus.CANCELLED, incident.getStatus());
        assertNotNull(incident.getClosedAt());
    }
}
