package com.aura.shared.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Email} value object.
 */
@Tag("unit")
@DisplayName("Email Value Object")
class EmailTest {

    @Test
    @DisplayName("should create a valid email and normalize to lowercase")
    void shouldCreateValidEmail() {
        Email email = Email.of("User@Example.COM");
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("should trim whitespace during creation")
    void shouldTrimWhitespace() {
        Email email = Email.of("  test@aura.com  ");
        assertThat(email.value()).isEqualTo("test@aura.com");
    }

    @Test
    @DisplayName("should throw when email is null")
    void shouldThrowWhenNull() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("blank");
    }

    @Test
    @DisplayName("should throw when email is blank")
    void shouldThrowWhenBlank() {
        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "missing@domain", "@nodomain.com", "spaces in@email.com"})
    @DisplayName("should throw for malformed email formats")
    void shouldThrowForMalformedEmails(String invalid) {
        assertThatThrownBy(() -> Email.of(invalid))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("two emails with the same normalized value should be equal")
    void shouldBeEqual() {
        Email a = Email.of("test@aura.com");
        Email b = Email.of("TEST@AURA.COM");
        assertThat(a).isEqualTo(b);
    }
}
