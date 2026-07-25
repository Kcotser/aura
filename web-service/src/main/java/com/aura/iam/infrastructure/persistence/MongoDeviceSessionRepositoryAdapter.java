package com.aura.iam.infrastructure.persistence;

import com.aura.iam.domain.model.DeviceSession;
import com.aura.iam.domain.model.DeviceSessionId;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.DeviceSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the domain {@link DeviceSessionRepository} port
 * using Spring Data MongoDB.
 */
@Repository
public class MongoDeviceSessionRepositoryAdapter implements DeviceSessionRepository {

    private final SpringDataDeviceSessionRepository springDataRepository;

    public MongoDeviceSessionRepositoryAdapter(SpringDataDeviceSessionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public DeviceSession save(DeviceSession session) {
        DeviceSessionDocument doc = DeviceSessionDocumentMapper.toDocument(session);
        DeviceSessionDocument saved = springDataRepository.save(doc);
        return DeviceSessionDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<DeviceSession> findById(DeviceSessionId id) {
        return springDataRepository.findById(id.value())
                .map(DeviceSessionDocumentMapper::toDomain);
    }

    @Override
    public Optional<DeviceSession> findByRefreshTokenHash(String refreshTokenHash) {
        return springDataRepository.findByRefreshTokenHash(refreshTokenHash)
                .map(DeviceSessionDocumentMapper::toDomain);
    }

    @Override
    public List<DeviceSession> findAllByUserId(UserId userId) {
        return springDataRepository.findAllByUserId(userId.value())
                .stream()
                .map(DeviceSessionDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<DeviceSession> sessions) {
        List<DeviceSessionDocument> docs = sessions.stream()
                .map(DeviceSessionDocumentMapper::toDocument)
                .toList();
        springDataRepository.saveAll(docs);
    }
}
