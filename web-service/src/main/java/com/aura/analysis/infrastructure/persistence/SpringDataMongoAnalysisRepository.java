package com.aura.analysis.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data Mongo repository interface for {@link AnalysisJobDocument}.
 */
@Repository
public interface SpringDataMongoAnalysisRepository extends MongoRepository<AnalysisJobDocument, String> {

    Optional<AnalysisJobDocument> findByIncidentId(String incidentId);
}
