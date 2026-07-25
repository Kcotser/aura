package com.aura.evidence.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.util.Objects;
import java.util.UUID;

public class EvidenceAssetId {

    private final String value;

    private EvidenceAssetId(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("Evidence asset ID must not be blank");
        }
        this.value = value.trim();
    }

    public static EvidenceAssetId generate() {
        return new EvidenceAssetId(UUID.randomUUID().toString());
    }

    public static EvidenceAssetId of(String value) {
        return new EvidenceAssetId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvidenceAssetId that = (EvidenceAssetId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
