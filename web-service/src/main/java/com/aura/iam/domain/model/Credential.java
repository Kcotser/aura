package com.aura.iam.domain.model;

/**
 * Value Object holding secondary authentication credential data for a User.
 *
 * <p>Embedded within the {@link User} aggregate. The PIN is stored as a
 * BCrypt hash — the raw PIN is never persisted anywhere.
 */
public record Credential(
        String pinHash,
        boolean biometricEnabled
) {

    /** Returns a Credential with no PIN and biometrics disabled (initial state). */
    public static Credential empty() {
        return new Credential(null, false);
    }

    /** Returns a new Credential with the given PIN hash set. */
    public Credential withPinHash(String pinHash) {
        return new Credential(pinHash, this.biometricEnabled);
    }

    /** Returns a new Credential with biometric status updated. */
    public Credential withBiometricEnabled(boolean enabled) {
        return new Credential(this.pinHash, enabled);
    }

    /** Returns true if a PIN has been configured. */
    public boolean hasPinConfigured() {
        return pinHash != null && !pinHash.isBlank();
    }
}
