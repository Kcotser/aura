package com.aura.directory.infrastructure.persistence;

import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.model.InstitutionId;
import com.aura.directory.domain.model.InstitutionType;
import com.aura.directory.domain.repository.InstitutionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the InstitutionRepository port using Spring Data MongoDB.
 */
@Repository
public class InstitutionRepositoryImpl implements InstitutionRepository {

    private final SpringDataMongoInstitutionRepository springDataRepository;
    private final InstitutionDocumentMapper mapper;

    public InstitutionRepositoryImpl(
            SpringDataMongoInstitutionRepository springDataRepository,
            InstitutionDocumentMapper mapper
    ) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Institution save(Institution institution) {
        InstitutionDocument doc = mapper.toDocument(institution);
        InstitutionDocument saved = springDataRepository.save(doc);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Institution> findById(InstitutionId id) {
        return springDataRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<Institution> findByCode(String code) {
        return springDataRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Institution> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Institution> findByType(InstitutionType type) {
        return springDataRepository.findByType(type).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
