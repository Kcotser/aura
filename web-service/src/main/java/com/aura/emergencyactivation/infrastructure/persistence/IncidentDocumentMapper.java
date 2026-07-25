package com.aura.emergencyactivation.infrastructure.persistence;

import com.aura.emergencyactivation.domain.model.GpsLocation;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.StatusTransition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class IncidentDocumentMapper {

    public IncidentDocument toDocument(Incident incident) {
        if (incident == null) {
            return null;
        }

        List<IncidentDocument.StatusTransitionDocument> history = incident.getStatusHistory().stream()
                .map(st -> IncidentDocument.StatusTransitionDocument.builder()
                        .fromStatus(st.fromStatus())
                        .toStatus(st.toStatus())
                        .timestamp(st.timestamp())
                        .reason(st.reason())
                        .build())
                .toList();

        return IncidentDocument.builder()
                .id(incident.getId().getValue())
                .userId(incident.getUserId())
                .status(incident.getStatus())
                .latitude(incident.getGpsLocation().getLatitude())
                .longitude(incident.getGpsLocation().getLongitude())
                .activatedAt(incident.getActivatedAt())
                .statusHistory(history)
                .closedAt(incident.getClosedAt())
                .build();
    }

    public Incident toDomain(IncidentDocument doc) {
        if (doc == null) {
            return null;
        }

        List<StatusTransition> history = doc.getStatusHistory() != null
                ? doc.getStatusHistory().stream()
                .map(st -> new StatusTransition(st.getFromStatus(), st.getToStatus(), st.getTimestamp(), st.getReason()))
                .toList()
                : Collections.emptyList();

        return Incident.reconstitute(
                IncidentId.of(doc.getId()),
                doc.getUserId(),
                doc.getStatus(),
                new GpsLocation(doc.getLatitude(), doc.getLongitude()),
                doc.getActivatedAt(),
                history,
                doc.getClosedAt()
        );
    }
}
