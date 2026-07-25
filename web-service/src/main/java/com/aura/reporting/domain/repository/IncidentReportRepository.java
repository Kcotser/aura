package com.aura.reporting.domain.repository;

import com.aura.reporting.domain.model.IncidentReport;
import com.aura.reporting.domain.model.IncidentReportId;

import java.util.Optional;

/**
 * Domain repository port for {@link IncidentReport} aggregate.
 */
public interface IncidentReportRepository {

    IncidentReport save(IncidentReport report);

    Optional<IncidentReport> findById(IncidentReportId id);

    Optional<IncidentReport> findByIncidentId(String incidentId);
}
