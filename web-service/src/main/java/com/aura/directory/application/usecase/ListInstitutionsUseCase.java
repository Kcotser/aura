package com.aura.directory.application.usecase;

import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.model.InstitutionType;
import com.aura.directory.domain.repository.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service orchestrating retrieval of institutions list.
 */
@Service
public class ListInstitutionsUseCase {

    private final InstitutionRepository repository;

    public ListInstitutionsUseCase(InstitutionRepository repository) {
        this.repository = repository;
    }

    public List<Institution> execute(InstitutionType type) {
        if (type != null) {
            return repository.findByType(type);
        }
        return repository.findAll();
    }
}
