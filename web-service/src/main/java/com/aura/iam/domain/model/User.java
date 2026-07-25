package com.aura.iam.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;
import com.aura.shared.domain.model.AuditMetadata;
import com.aura.shared.domain.model.Email;

/**
 * Aggregate Root for the IAM bounded context.
 *
 * <p>Encapsulates all identity and credential logic for a registered user.
 * This class is persistence-ignorant: no Spring or MongoDB annotations allowed here.
 *
 * <p>All state changes are made through explicit methods that enforce invariants.
 */
public class User {

    private final UserId id;
    private Email email;
    private String hashedPassword;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean active;
    private Credential credential;
    private AuditMetadata auditMetadata;

    /**
     * Private constructor — use {@link #register} factory method to create new users.
     */
    private User(
            UserId id,
            Email email,
            String hashedPassword,
            String firstName,
            String lastName,
            String phoneNumber,
            boolean active,
            Credential credential,
            AuditMetadata auditMetadata
    ) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.credential = credential;
        this.auditMetadata = auditMetadata;
    }

    /**
     * Factory method to create and validate a new User registration.
     *
     * @param email          validated Email value object
     * @param hashedPassword BCrypt hash of the user's password
     * @param firstName      user's first name
     * @param lastName       user's last name
     * @param phoneNumber    optional phone number
     * @return a new, active User in initial state
     * @throws BusinessRuleViolationException if any invariant is violated
     */
    public static User register(
            Email email,
            String hashedPassword,
            String firstName,
            String lastName,
            String phoneNumber
    ) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        if (hashedPassword == null || hashedPassword.isBlank()) {
            throw new BusinessRuleViolationException("Hashed password must not be blank");
        }
        return new User(
                UserId.generate(),
                email,
                hashedPassword,
                firstName.trim(),
                lastName.trim(),
                phoneNumber,
                true,
                Credential.empty(),
                AuditMetadata.now()
        );
    }

    /**
     * Reconstitutes a User from persistence (infrastructure use only).
     */
    public static User reconstitute(
            UserId id,
            Email email,
            String hashedPassword,
            String firstName,
            String lastName,
            String phoneNumber,
            boolean active,
            Credential credential,
            AuditMetadata auditMetadata
    ) {
        return new User(id, email, hashedPassword, firstName, lastName,
                phoneNumber, active, credential, auditMetadata);
    }

    /**
     * Configures or updates the backup PIN.
     *
     * @param pinHash BCrypt hash of the PIN
     */
    public void configurePin(String pinHash) {
        if (pinHash == null || pinHash.isBlank()) {
            throw new BusinessRuleViolationException("PIN hash must not be blank");
        }
        this.credential = this.credential.withPinHash(pinHash);
        this.auditMetadata = this.auditMetadata.updated();
    }

    /**
     * Updates the user's profile fields. Null values are ignored (partial update).
     */
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        if (firstName != null) {
            validateName(firstName, "First name");
            this.firstName = firstName.trim();
        }
        if (lastName != null) {
            validateName(lastName, "Last name");
            this.lastName = lastName.trim();
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        this.auditMetadata = this.auditMetadata.updated();
    }

    /** Marks this account as inactive (soft delete). */
    public void deactivate() {
        this.active = false;
        this.auditMetadata = this.auditMetadata.updated();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void validateName(String name, String fieldLabel) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException(fieldLabel + " must not be blank");
        }
        if (name.trim().length() < 2) {
            throw new BusinessRuleViolationException(fieldLabel + " must be at least 2 characters");
        }
    }

    // -------------------------------------------------------------------------
    // Getters (read-only access — no setters to preserve invariants)
    // -------------------------------------------------------------------------

    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isActive() { return active; }
    public Credential getCredential() { return credential; }
    public AuditMetadata getAuditMetadata() { return auditMetadata; }
}
