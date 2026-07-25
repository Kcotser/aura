package com.aura.reporting.application.usecase;

import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.repository.IncidentReportRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Use case to retrieve an incident report draft by incidentId.
 */
@Service
public class GetReportUseCase {

    private final IncidentReportRepository repository;

    public GetReportUseCase(IncidentReportRepository repository) {
        this.repository = repository;
    }

    public IncidentReport execute(String incidentId) {
        return repository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", incidentId));
    }
}
