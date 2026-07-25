package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.ConfigurePinCommand;
import com.aura.iam.domain.event.PinConfiguredEvent;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.BusinessRuleViolationException;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Configures or updates a user's backup PIN.
 *
 * <p>Validates PIN format (4-6 digits), hashes it with BCrypt, and persists.
 */
@Service
public class ConfigurePinUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfigurePinUseCase.class);
    private static final String PIN_REGEX = "^\\d{4,6}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public ConfigurePinUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public void execute(ConfigurePinCommand command) {
        if (command.pin() == null || !command.pin().matches(PIN_REGEX)) {
            throw new BusinessRuleViolationException("PIN must be 4 to 6 digits");
        }

        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userId()));

        String pinHash = passwordEncoder.encode(command.pin());
        user.configurePin(pinHash);
        userRepository.save(user);

        log.info("PIN configured for userId={}", command.userId());
        eventPublisher.publishEvent(new PinConfiguredEvent(user.getId()));
    }
}
