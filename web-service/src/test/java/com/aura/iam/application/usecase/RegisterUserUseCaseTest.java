package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.RegisterUserCommand;
import com.aura.iam.application.dto.UserProfileResult;
import com.aura.iam.domain.event.UserRegisteredEvent;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.BusinessRuleViolationException;
import com.aura.shared.domain.model.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RegisterUserUseCase}.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase")
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PasswordEncoder passwordEncoder;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4); // Low cost for tests
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder, eventPublisher);
    }

    @Test
    @DisplayName("should register a user and publish UserRegisteredEvent")
    void shouldRegisterUserSuccessfully() {
        RegisterUserCommand command = new RegisterUserCommand(
                "maria@aura.com", "securepassword123", "Maria", "Lopez", "+51999999999"
        );

        when(userRepository.existsByEmail(Email.of(command.email()))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResult result = useCase.execute(command);

        assertThat(result.email()).isEqualTo("maria@aura.com");
        assertThat(result.firstName()).isEqualTo("Maria");
        assertThat(result.pinConfigured()).isFalse();

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().email()).isEqualTo("maria@aura.com");
    }

    @Test
    @DisplayName("should throw when email is already registered")
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterUserCommand command = new RegisterUserCommand(
                "existing@aura.com", "password123", "Ana", "Garcia", null
        );

        when(userRepository.existsByEmail(Email.of(command.email()))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should hash the password before saving")
    void shouldHashPassword() {
        RegisterUserCommand command = new RegisterUserCommand(
                "new@aura.com", "plainpassword", "Carmen", "Torres", null
        );

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            // Verify the password is hashed (not plain text)
            assertThat(user.getHashedPassword()).isNotEqualTo("plainpassword");
            assertThat(user.getHashedPassword()).startsWith("$2a$");
            return user;
        });

        useCase.execute(command);
        verify(userRepository).save(any(User.class));
    }
}
