package com.aura.directory.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.util.Objects;

/**
 * Aggregate Root for the Directory bounded context.
 * Represents a support institution or helpline.
 */
public class Institution {
    private final InstitutionId id;
    private final String code;
    private final String name;
    private final InstitutionType type;
    private final String phoneNumber;
    private final String address;
    private final GeoLocation geoLocation;

    private Institution(
            InstitutionId id,
            String code,
            String name,
            InstitutionType type,
            String phoneNumber,
            String address,
            GeoLocation geoLocation
    ) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleViolationException("Institution code must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("Institution name must not be blank");
        }
        if (type == null) {
            throw new BusinessRuleViolationException("Institution type must not be null");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BusinessRuleViolationException("Institution phone number must not be blank");
        }
        this.id = Objects.requireNonNull(id, "InstitutionId must not be null");
        this.code = code.trim().toLowerCase();
        this.name = name.trim();
        this.type = type;
        this.phoneNumber = phoneNumber.trim();
        this.address = address != null ? address.trim() : null;
        this.geoLocation = geoLocation;
    }

    public static Institution create(
            String code,
            String name,
            InstitutionType type,
            String phoneNumber,
            String address,
            GeoLocation geoLocation
    ) {
        return new Institution(
                InstitutionId.generate(),
                code,
                name,
                type,
                phoneNumber,
                address,
                geoLocation
        );
    }

    public static Institution reconstitute(
            InstitutionId id,
            String code,
            String name,
            InstitutionType type,
            String phoneNumber,
            String address,
            GeoLocation geoLocation
    ) {
        return new Institution(id, code, name, type, phoneNumber, address, geoLocation);
    }

    public InstitutionId getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public InstitutionType getType() {
        return type;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }
}
