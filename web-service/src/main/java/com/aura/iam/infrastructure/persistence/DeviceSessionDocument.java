package com.aura.iam.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for the device_sessions collection.
 */
@Document(collection = "device_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSessionDocument {

    @Id
    private String id;

    private String userId;

    @Indexed
    private String refreshTokenHash;

    private String deviceId;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean revoked;
}
