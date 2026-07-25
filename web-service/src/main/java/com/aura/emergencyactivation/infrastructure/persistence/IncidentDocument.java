package com.aura.emergencyactivation.infrastructure.persistence;

import com.aura.emergencyactivation.domain.model.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document for the incidents collection.
 */
@Document(collection = "incidents")
@CompoundIndexes({
        @CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDocument {

    @Id
    private String id;
    private String userId;
    private IncidentStatus status;

    private double latitude;
    private double longitude;

    private Instant activatedAt;
    private List<StatusTransitionDocument> statusHistory;
    private Instant closedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusTransitionDocument {
        private IncidentStatus fromStatus;
        private IncidentStatus toStatus;
        private Instant timestamp;
        private String reason;
    }
}
