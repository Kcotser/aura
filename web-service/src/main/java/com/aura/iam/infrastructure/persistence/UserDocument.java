package com.aura.iam.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for the users collection.
 *
 * <p>Intentionally separate from the domain {@link com.aura.iam.domain.model.User}
 * aggregate to keep the domain persistence-ignorant.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String hashedPassword;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean active;

    // Embedded credential
    private String pinHash;
    private boolean biometricEnabled;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
}
