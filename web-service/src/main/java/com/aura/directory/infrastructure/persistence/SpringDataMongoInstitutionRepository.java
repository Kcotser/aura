package com.aura.directory.infrastructure.persistence;

import com.aura.directory.domain.model.InstitutionType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for InstitutionDocument.
 */
public interface SpringDataMongoInstitutionRepository extends MongoRepository<InstitutionDocument, String> {
    Optional<InstitutionDocument> findByCode(String code);
    List<InstitutionDocument> findByType(InstitutionType type);
}
