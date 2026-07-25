package com.aura.directory.application.mapper;

import com.aura.directory.application.dto.InstitutionResult;
import com.aura.directory.domain.model.Institution;
import org.springframework.stereotype.Component;

/**
 * Mapper component to convert Institution domain entities to DTOs.
 */
@Component
public class InstitutionMapper {

    public InstitutionResult toResult(Institution institution) {
        return InstitutionResult.fromDomain(institution);
    }
}
