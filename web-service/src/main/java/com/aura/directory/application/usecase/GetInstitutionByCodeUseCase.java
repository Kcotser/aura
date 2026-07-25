package com.aura.directory.application.usecase;

import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.repository.InstitutionRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service orchestrating lookup of a single institution by its code.
 * Falls back to "linea-100" if the code does not exist.
 */
@Service
public class GetInstitutionByCodeUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetInstitutionByCodeUseCase.class);
    private static final String FALLBACK_CODE = "linea-100";

    private final InstitutionRepository repository;

    public GetInstitutionByCodeUseCase(InstitutionRepository repository) {
        this.repository = repository;
    }

    public Institution execute(String code) {
        if (code == null || code.isBlank()) {
            log.warn("Null or blank institution code requested, returning fallback: {}", FALLBACK_CODE);
            return getFallbackInstitution("blank");
        }

        String normalizedCode = code.trim().toLowerCase();
        return repository.findByCode(normalizedCode)
                .orElseGet(() -> {
                    log.warn("Institution code '{}' not found, falling back to '{}'", code, FALLBACK_CODE);
                    return getFallbackInstitution(code);
                });
    }

    private Institution getFallbackInstitution(String originalCode) {
        return repository.findByCode(FALLBACK_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", originalCode));
    }
}
