package com.aura.emergencyactivation.infrastructure.persistence;

import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IncidentRepositoryImpl implements IncidentRepository {

    private final SpringDataMongoIncidentRepository springDataRepository;
    private final IncidentDocumentMapper mapper;

    public IncidentRepositoryImpl(SpringDataMongoIncidentRepository springDataRepository, IncidentDocumentMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentDocument doc = mapper.toDocument(incident);
        IncidentDocument saved = springDataRepository.save(doc);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Incident> findById(IncidentId id) {
        return springDataRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<Incident> findByUserId(String userId, IncidentStatus statusFilter, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "activatedAt"));
        if (statusFilter != null) {
            return springDataRepository.findByUserIdAndStatus(userId, statusFilter, pageable)
                    .map(mapper::toDomain)
                    .getContent();
        } else {
            return springDataRepository.findByUserId(userId, pageable)
                    .map(mapper::toDomain)
                    .getContent();
        }
    }

    @Override
    public long countByUserId(String userId, IncidentStatus statusFilter) {
        if (statusFilter != null) {
            return springDataRepository.countByUserIdAndStatus(userId, statusFilter);
        } else {
            return springDataRepository.countByUserId(userId);
        }
    }
}
