package com.aura.reporting.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data Mongo repository interface for {@link IncidentReportDocument}.
 */
@Repository
public interface SpringDataMongoReportRepository extends MongoRepository<IncidentReportDocument, String> {

    Optional<IncidentReportDocument> findByIncidentId(String incidentId);
}
