package com.aura.iam.infrastructure.persistence;

import com.aura.iam.domain.model.Credential;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.shared.domain.model.AuditMetadata;
import com.aura.shared.domain.model.Email;

/**
 * Bidirectional mapper between the domain {@link User} aggregate and
 * the persistence {@link UserDocument}.
 *
 * <p>This class is the single point of translation — the domain never
 * knows about MongoDB, and MongoDB documents never bleed into the domain.
 */
class UserDocumentMapper {

    private UserDocumentMapper() {
        // Utility class
    }

    static UserDocument toDocument(User user) {
        return UserDocument.builder()
                .id(user.getId().value())
                .email(user.getEmail().value())
                .hashedPassword(user.getHashedPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .pinHash(user.getCredential().pinHash())
                .biometricEnabled(user.getCredential().biometricEnabled())
                .createdAt(user.getAuditMetadata().createdAt())
                .updatedAt(user.getAuditMetadata().updatedAt())
                .build();
    }

    static User toDomain(UserDocument doc) {
        return User.reconstitute(
                UserId.of(doc.getId()),
                Email.of(doc.getEmail()),
                doc.getHashedPassword(),
                doc.getFirstName(),
                doc.getLastName(),
                doc.getPhoneNumber(),
                doc.isActive(),
                new Credential(doc.getPinHash(), doc.isBiometricEnabled()),
                new AuditMetadata(doc.getCreatedAt(), doc.getUpdatedAt())
        );
    }
}
