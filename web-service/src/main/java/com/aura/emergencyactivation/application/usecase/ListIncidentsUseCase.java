package com.aura.emergencyactivation.application.usecase;

import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.emergencyactivation.domain.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case to retrieve a paginated list of incidents for an authenticated user.
 */
@Service
public class ListIncidentsUseCase {

    private final IncidentRepository incidentRepository;

    public ListIncidentsUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> execute(String userId, IncidentStatus statusFilter, int page, int size) {
        return incidentRepository.findByUserId(userId, statusFilter, page, size);
    }

    public long count(String userId, IncidentStatus statusFilter) {
        return incidentRepository.countByUserId(userId, statusFilter);
    }
}
