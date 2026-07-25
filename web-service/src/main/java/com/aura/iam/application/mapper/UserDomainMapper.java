package com.aura.iam.application.mapper;

import com.aura.iam.application.dto.UserProfileResult;
import com.aura.iam.domain.model.User;

/**
 * Maps between the User domain model and application-layer DTOs.
 */
public class UserDomainMapper {

    private UserDomainMapper() {
        // Utility class
    }

    public static UserProfileResult toProfileResult(User user) {
        return new UserProfileResult(
                user.getId().value(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getCredential().hasPinConfigured(),
                user.getCredential().biometricEnabled(),
                user.getAuditMetadata().createdAt()
        );
    }
}
