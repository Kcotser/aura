package com.aura.emergencyactivation.domain.repository;

import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentId;
import com.aura.emergencyactivation.domain.model.IncidentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link Incident} aggregate operations.
 */
public interface IncidentRepository {

    Incident save(Incident incident);

    Optional<Incident> findById(IncidentId id);

    List<Incident> findByUserId(String userId, IncidentStatus statusFilter, int page, int size);

    long countByUserId(String userId, IncidentStatus statusFilter);
}
