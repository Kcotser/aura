package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.ValidatePinCommand;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.AuthenticationFailedException;
import com.aura.shared.domain.exception.BusinessRuleViolationException;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Validates a user's backup PIN (used as biometric fallback).
 */
@Service
public class ValidatePinUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ValidatePinUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @return true if the PIN is valid
     * @throws AuthenticationFailedException if PIN is wrong
     * @throws BusinessRuleViolationException if no PIN has been configured
     */
    public boolean execute(ValidatePinCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userId()));

        if (!user.getCredential().hasPinConfigured()) {
            throw new BusinessRuleViolationException("No PIN has been configured for this account");
        }

        if (!passwordEncoder.matches(command.pin(), user.getCredential().pinHash())) {
            throw new AuthenticationFailedException("Invalid PIN");
        }

        return true;
    }
}
