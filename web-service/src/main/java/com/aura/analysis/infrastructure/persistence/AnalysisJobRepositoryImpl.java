package com.aura.analysis.infrastructure.persistence;

import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.model.AnalysisJobId;
import com.aura.analysis.domain.repository.AnalysisJobRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of {@link AnalysisJobRepository} domain port using MongoDB.
 */
@Component
public class AnalysisJobRepositoryImpl implements AnalysisJobRepository {

    private final SpringDataMongoAnalysisRepository repository;
    private final AnalysisDocumentMapper mapper;

    public AnalysisJobRepositoryImpl(SpringDataMongoAnalysisRepository repository, AnalysisDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AnalysisJob save(AnalysisJob job) {
        AnalysisJobDocument doc = mapper.toDocument(job);
        AnalysisJobDocument saved = repository.save(doc);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AnalysisJob> findById(AnalysisJobId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<AnalysisJob> findByIncidentId(String incidentId) {
        return repository.findByIncidentId(incidentId).map(mapper::toDomain);
    }
}
