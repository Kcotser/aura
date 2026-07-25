package com.aura.iam.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository interface for {@link DeviceSessionDocument}.
 * Used internally by {@link MongoDeviceSessionRepositoryAdapter}.
 */
interface SpringDataDeviceSessionRepository extends MongoRepository<DeviceSessionDocument, String> {

    Optional<DeviceSessionDocument> findByRefreshTokenHash(String refreshTokenHash);

    List<DeviceSessionDocument> findAllByUserId(String userId);
}
