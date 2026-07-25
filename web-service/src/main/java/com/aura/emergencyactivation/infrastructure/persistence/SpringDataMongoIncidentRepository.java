package com.aura.emergencyactivation.infrastructure.persistence;

import com.aura.emergencyactivation.domain.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoIncidentRepository extends MongoRepository<IncidentDocument, String> {

    Page<IncidentDocument> findByUserId(String userId, Pageable pageable);

    Page<IncidentDocument> findByUserIdAndStatus(String userId, IncidentStatus status, Pageable pageable);

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, IncidentStatus status);
}
