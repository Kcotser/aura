package com.aura.directory.application.dto;

import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.model.InstitutionType;

/**
 * Data Transfer Object for institutional catalog details.
 */
public record InstitutionResult(
        String id,
        String code,
        String name,
        InstitutionType type,
        String phoneNumber,
        String address,
        Double latitude,
        Double longitude
) {
    public static InstitutionResult fromDomain(Institution institution) {
        if (institution == null) {
            return null;
        }
        return new InstitutionResult(
                institution.getId().getValue(),
                institution.getCode(),
                institution.getName(),
                institution.getType(),
                institution.getPhoneNumber(),
                institution.getAddress(),
                institution.getGeoLocation() != null ? institution.getGeoLocation().latitude() : null,
                institution.getGeoLocation() != null ? institution.getGeoLocation().longitude() : null
        );
    }
}
