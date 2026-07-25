package com.aura.emergencyactivation.interfaces.rest.response;

import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentStatus;

import java.time.Instant;
import java.util.List;

public record IncidentResponse(
        String id,
        String userId,
        IncidentStatus status,
        GpsLocationResponse gpsLocation,
        Instant activatedAt,
        List<StatusTransitionResponse> statusHistory,
        Instant closedAt
) {
    public static IncidentResponse fromDomain(Incident incident) {
        GpsLocationResponse loc = new GpsLocationResponse(
                incident.getGpsLocation().getLatitude(),
                incident.getGpsLocation().getLongitude()
        );
        List<StatusTransitionResponse> history = incident.getStatusHistory().stream()
                .map(st -> new StatusTransitionResponse(
                        st.fromStatus(),
                        st.toStatus(),
                        st.timestamp(),
                        st.reason()
                )).toList();

        return new IncidentResponse(
                incident.getId().getValue(),
                incident.getUserId(),
                incident.getStatus(),
                loc,
                incident.getActivatedAt(),
                history,
                incident.getClosedAt()
        );
    }
}
