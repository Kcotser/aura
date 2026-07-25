package com.aura.emergencyactivation.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.util.Objects;

/**
 * Immutable Value Object representing a GPS coordinate (latitude and longitude).
 */
public class GpsLocation {

    private final double latitude;
    private final double longitude;

    public GpsLocation(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new BusinessRuleViolationException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new BusinessRuleViolationException("Longitude must be between -180 and 180 degrees");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GpsLocation that = (GpsLocation) o;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("(%.6f, %.6f)", latitude, longitude);
    }
}
