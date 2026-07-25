package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.RegisterUserCommand;
import com.aura.iam.application.dto.UserProfileResult;
import com.aura.iam.application.mapper.UserDomainMapper;
import com.aura.iam.domain.event.UserRegisteredEvent;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.BusinessRuleViolationException;
import com.aura.shared.domain.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the user registration flow.
 *
 * <p>Validates uniqueness of email, hashes the password, creates the User
 * aggregate, persists it, and publishes a {@link UserRegisteredEvent}.
 */
@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public UserProfileResult execute(RegisterUserCommand command) {
        Email email = Email.of(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleViolationException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        User user = User.register(
                email,
                hashedPassword,
                command.firstName(),
                command.lastName(),
                command.phoneNumber()
        );

        User saved = userRepository.save(user);
        log.info("User registered: userId={}", saved.getId());

        eventPublisher.publishEvent(
                new UserRegisteredEvent(saved.getId(), saved.getEmail().value(), saved.getFirstName())
        );

        return UserDomainMapper.toProfileResult(saved);
    }
}
