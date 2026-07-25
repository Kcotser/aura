package com.aura.directory.domain.repository;

import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.model.InstitutionId;
import com.aura.directory.domain.model.InstitutionType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface port for the Institution aggregate.
 */
public interface InstitutionRepository {
    Institution save(Institution institution);
    Optional<Institution> findById(InstitutionId id);
    Optional<Institution> findByCode(String code);
    List<Institution> findAll();
    List<Institution> findByType(InstitutionType type);
    long count();
}
