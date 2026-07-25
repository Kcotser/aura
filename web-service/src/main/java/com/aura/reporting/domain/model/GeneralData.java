package com.aura.reporting.domain.model;

import java.time.Instant;

/**
 * Value object representing basic metadata and GPS coordinates of an incident report.
 */
public record GeneralData(
        Instant timestamp,
        Double latitude,
        Double longitude
) {

    public GeneralData {
        timestamp = timestamp != null ? timestamp : Instant.now();
        latitude = latitude != null ? latitude : 0.0;
        longitude = longitude != null ? longitude : 0.0;
    }
}
