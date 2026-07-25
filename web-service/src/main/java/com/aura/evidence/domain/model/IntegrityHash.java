package com.aura.evidence.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.util.Objects;

public class IntegrityHash {

    private final String sha256;

    public IntegrityHash(String sha256) {
        if (sha256 == null || sha256.isBlank()) {
            throw new BusinessRuleViolationException("Integrity hash must not be blank");
        }
        if (sha256.trim().length() != 64) {
            throw new BusinessRuleViolationException("SHA-256 hash must be exactly 64 hexadecimal characters");
        }
        this.sha256 = sha256.trim().toLowerCase();
    }

    public String getSha256() {
        return sha256;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrityHash that = (IntegrityHash) o;
        return Objects.equals(sha256, that.sha256);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha256);
    }
}
