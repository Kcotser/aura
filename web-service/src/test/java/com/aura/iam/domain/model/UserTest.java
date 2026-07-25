package com.aura.iam.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;
import com.aura.shared.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link User} aggregate root.
 */
@Tag("unit")
@DisplayName("User Aggregate")
class UserTest {

    private static final String VALID_EMAIL = "test@aura.com";
    private static final String VALID_HASH = "$2a$12$someHashedPasswordValue";
    private static final String FIRST_NAME = "Maria";
    private static final String LAST_NAME = "Lopez";

    @Test
    @DisplayName("should register a new active user")
    void shouldRegisterNewUser() {
        User user = User.register(
                Email.of(VALID_EMAIL), VALID_HASH, FIRST_NAME, LAST_NAME, "+51999999999"
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail().value()).isEqualTo(VALID_EMAIL);
        assertThat(user.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(user.getLastName()).isEqualTo(LAST_NAME);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCredential().hasPinConfigured()).isFalse();
        assertThat(user.getAuditMetadata().createdAt()).isNotNull();
    }

    @Test
    @DisplayName("should throw when first name is too short")
    void shouldThrowWhenFirstNameTooShort() {
        assertThatThrownBy(() -> User.register(
                Email.of(VALID_EMAIL), VALID_HASH, "A", LAST_NAME, null
        )).isInstanceOf(BusinessRuleViolationException.class)
          .hasMessageContaining("First name");
    }

    @Test
    @DisplayName("should throw when hashed password is blank")
    void shouldThrowWhenHashedPasswordBlank() {
        assertThatThrownBy(() -> User.register(
                Email.of(VALID_EMAIL), "  ", FIRST_NAME, LAST_NAME, null
        )).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("should configure PIN successfully")
    void shouldConfigurePin() {
        User user = User.register(Email.of(VALID_EMAIL), VALID_HASH, FIRST_NAME, LAST_NAME, null);
        user.configurePin("$2a$12$someHashedPinValue");

        assertThat(user.getCredential().hasPinConfigured()).isTrue();
        assertThat(user.getCredential().pinHash()).isEqualTo("$2a$12$someHashedPinValue");
    }

    @Test
    @DisplayName("should throw when configuring blank PIN hash")
    void shouldThrowWhenPinHashBlank() {
        User user = User.register(Email.of(VALID_EMAIL), VALID_HASH, FIRST_NAME, LAST_NAME, null);
        assertThatThrownBy(() -> user.configurePin(""))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("should update profile fields correctly")
    void shouldUpdateProfile() {
        User user = User.register(Email.of(VALID_EMAIL), VALID_HASH, FIRST_NAME, LAST_NAME, null);
        user.updateProfile("Ana", "Garcia", "+51888888888");

        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(user.getLastName()).isEqualTo("Garcia");
        assertThat(user.getPhoneNumber()).isEqualTo("+51888888888");
    }

    @Test
    @DisplayName("should deactivate user on deletion request")
    void shouldDeactivateUser() {
        User user = User.register(Email.of(VALID_EMAIL), VALID_HASH, FIRST_NAME, LAST_NAME, null);
        user.deactivate();

        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("updateProfile with null fields should keep existing values")
    void shouldKeepExistingValuesOnNullUpdate() {
        User user = User.register(Email.of(VALID_EMAIL), VALID_HASH, FIRST_NAME, LAST_NAME, "+1234");
        user.updateProfile(null, null, null);

        assertThat(user.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(user.getLastName()).isEqualTo(LAST_NAME);
        assertThat(user.getPhoneNumber()).isEqualTo("+1234");
    }
}
