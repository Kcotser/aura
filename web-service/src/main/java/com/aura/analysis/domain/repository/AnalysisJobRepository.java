package com.aura.analysis.domain.repository;

import com.aura.analysis.domain.model.AnalysisJob;
import com.aura.analysis.domain.model.AnalysisJobId;

import java.util.Optional;

/**
 * Domain repository port for {@link AnalysisJob} operations.
 */
public interface AnalysisJobRepository {

    AnalysisJob save(AnalysisJob job);

    Optional<AnalysisJob> findById(AnalysisJobId id);

    Optional<AnalysisJob> findByIncidentId(String incidentId);
}
