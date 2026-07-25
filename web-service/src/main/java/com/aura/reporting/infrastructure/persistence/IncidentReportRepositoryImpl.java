package com.aura.reporting.infrastructure.persistence;

import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.model.IncidentReportId;
import com.aura.reporting.domain.repository.IncidentReportRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of {@link IncidentReportRepository} domain port using MongoDB.
 */
@Component
public class IncidentReportRepositoryImpl implements IncidentReportRepository {

    private final SpringDataMongoReportRepository repository;
    private final ReportDocumentMapper mapper;

    public IncidentReportRepositoryImpl(SpringDataMongoReportRepository repository, ReportDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public IncidentReport save(IncidentReport report) {
        IncidentReportDocument doc = mapper.toDocument(report);
        IncidentReportDocument saved = repository.save(doc);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<IncidentReport> findById(IncidentReportId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<IncidentReport> findByIncidentId(String incidentId) {
        return repository.findByIncidentId(incidentId).map(mapper::toDomain);
    }
}
