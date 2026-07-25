package com.aura.directory.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

/**
 * Immutable Value Object representing a GPS coordinate (latitude and longitude) for directory institutions.
 */
public record GeoLocation(double latitude, double longitude) {
    public GeoLocation {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new BusinessRuleViolationException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new BusinessRuleViolationException("Longitude must be between -180 and 180 degrees");
        }
    }
}
