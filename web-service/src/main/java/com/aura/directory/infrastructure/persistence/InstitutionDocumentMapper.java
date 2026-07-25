package com.aura.directory.infrastructure.persistence;

import com.aura.directory.domain.model.GeoLocation;
import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.model.InstitutionId;
import org.springframework.stereotype.Component;

/**
 * Maps between the Institution domain aggregate and InstitutionDocument database entity.
 */
@Component
public class InstitutionDocumentMapper {

    public InstitutionDocument toDocument(Institution domain) {
        if (domain == null) {
            return null;
        }
        return InstitutionDocument.builder()
                .id(domain.getId().getValue())
                .code(domain.getCode())
                .name(domain.getName())
                .type(domain.getType())
                .phoneNumber(domain.getPhoneNumber())
                .address(domain.getAddress())
                .latitude(domain.getGeoLocation() != null ? domain.getGeoLocation().latitude() : null)
                .longitude(domain.getGeoLocation() != null ? domain.getGeoLocation().longitude() : null)
                .build();
    }

    public Institution toDomain(InstitutionDocument doc) {
        if (doc == null) {
            return null;
        }
        GeoLocation geoLocation = null;
        if (doc.getLatitude() != null && doc.getLongitude() != null) {
            geoLocation = new GeoLocation(doc.getLatitude(), doc.getLongitude());
        }
        return Institution.reconstitute(
                InstitutionId.of(doc.getId()),
                doc.getCode(),
                doc.getName(),
                doc.getType(),
                doc.getPhoneNumber(),
                doc.getAddress(),
                geoLocation
        );
    }
}
