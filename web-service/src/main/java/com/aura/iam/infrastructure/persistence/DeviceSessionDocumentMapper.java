package com.aura.iam.infrastructure.persistence;

import com.aura.iam.domain.model.DeviceSession;
import com.aura.iam.domain.model.DeviceSessionId;
import com.aura.iam.domain.model.UserId;

/**
 * Bidirectional mapper between the domain {@link DeviceSession} entity and
 * the persistence {@link DeviceSessionDocument}.
 */
class DeviceSessionDocumentMapper {

    private DeviceSessionDocumentMapper() {
        // Utility class
    }

    static DeviceSessionDocument toDocument(DeviceSession session) {
        return DeviceSessionDocument.builder()
                .id(session.getId().value())
                .userId(session.getUserId().value())
                .refreshTokenHash(session.getRefreshTokenHash())
                .deviceId(session.getDeviceId())
                .issuedAt(session.getIssuedAt())
                .expiresAt(session.getExpiresAt())
                .revoked(session.isRevoked())
                .build();
    }

    static DeviceSession toDomain(DeviceSessionDocument doc) {
        return DeviceSession.reconstitute(
                DeviceSessionId.of(doc.getId()),
                UserId.of(doc.getUserId()),
                doc.getRefreshTokenHash(),
                doc.getDeviceId(),
                doc.getIssuedAt(),
                doc.getExpiresAt(),
                doc.isRevoked()
        );
    }
}
